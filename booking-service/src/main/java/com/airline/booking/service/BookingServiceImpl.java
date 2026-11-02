package com.airline.booking.service;

import com.airline.booking.domain.BookingStatus;
import com.airline.booking.domain.FlightStatus;
import com.airline.booking.dto.BookingDtos.BookingRequest;
import com.airline.booking.dto.BookingDtos.BookingResponse;
import com.airline.booking.dto.BookingDtos.AdminBookingResponse;
import com.airline.booking.dto.BookingDtos.FlightSnapshot;
import com.airline.booking.entity.Booking;
import com.airline.booking.entity.SeatAllocation;
import com.airline.booking.repository.BookingRepository;
import com.airline.booking.repository.SeatAllocationRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BookingServiceImpl implements BookingService {
  private final BookingRepository bookings;
  private final SeatAllocationRepository seats;
  private final RestClient flightClient;

  public BookingServiceImpl(
      BookingRepository bookings,
      SeatAllocationRepository seats,
      @Qualifier("flightRestClientBuilder") RestClient.Builder builder) {
    this.bookings = bookings;
    this.seats = seats;
    this.flightClient = builder.baseUrl("http://flight-service").build();
  }

  @Override
  public List<Integer> occupied(Long flightId) {
    return seats.findByFlightId(flightId).stream()
        .map(allocation -> allocation.seatNumber)
        .sorted()
        .toList();
  }

  @Override
  public List<BookingResponse> mine(Long userId) {
    return bookings.findByUserIdOrderByBookedAtDesc(userId).stream()
        .map(this::toResponse)
        .toList();
  }

  @Override
  public List<AdminBookingResponse> adminBookings(Long flightId, BookingStatus status) {
    if (flightId != null && flightId < 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Flight ID must be positive");
    }
    return bookings.findAllByOrderByBookedAtDesc().stream()
        .filter(booking -> flightId == null || booking.flightId.equals(flightId))
        .filter(booking -> status == null || booking.status == status)
        .map(this::toAdminResponse)
        .toList();
  }

  @Override
  @Transactional
  public BookingResponse book(Long userId, BookingRequest request) {
    FlightSnapshot flight = lookupFlight(request.flightId());
    LocalDateTime nowUtc = LocalDateTime.now(Clock.systemUTC());
    boolean flightIsBookable = flight != null
        && flight.status() == FlightStatus.OPEN
        && flight.departureTime().isAfter(nowUtc);
    if (!flightIsBookable) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Flight is not open for booking");
    }
    if (request.seatNumber() > flight.capacity()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat number exceeds capacity");
    }

    Booking booking = bookings.saveAndFlush(
        new Booking(userId, flight.id(), request.seatNumber(), flight.price()));
    try {
      // The database's unique flight/seat constraint handles simultaneous booking
      // attempts.
      seats.saveAndFlush(new SeatAllocation(booking.id, flight.id(), request.seatNumber()));
    } catch (DataIntegrityViolationException e) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Seat already booked");
    }
    return toResponse(booking);
  }

  @Override
  @Transactional
  public BookingResponse cancel(Long userId, Long id) {
    Booking booking = bookings.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));

    if (!booking.userId.equals(userId)) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "This booking belongs to another customer");
    }
    if (booking.status != BookingStatus.CONFIRMED) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Already cancelled");
    }

    FlightSnapshot flight = lookupFlight(booking.flightId);
    if (!flight.departureTime().isAfter(LocalDateTime.now(Clock.systemUTC()))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Past flights cannot be cancelled");
    }

    booking.status = BookingStatus.CANCELLED;
    seats.deleteById(booking.id);
    return toResponse(bookings.save(booking));
  }

  private BookingResponse toResponse(Booking booking) {
    return new BookingResponse(
        booking.id,
        booking.flightId,
        booking.seatNumber,
        booking.fareAtBooking,
        booking.status,
        booking.bookedAt);
  }

  private AdminBookingResponse toAdminResponse(Booking booking) {
    return new AdminBookingResponse(
        booking.id,
        booking.userId,
        booking.flightId,
        booking.seatNumber,
        booking.fareAtBooking,
        booking.status,
        booking.bookedAt);
  }

  private FlightSnapshot lookupFlight(Long flightId) {
    try {
      return flightClient.get()
          .uri("/api/flights/{id}", flightId)
          .retrieve()
          .body(FlightSnapshot.class);
    } catch (HttpClientErrorException.NotFound e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found");
    } catch (RestClientException e) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Flight service unavailable");
    }
  }
}
