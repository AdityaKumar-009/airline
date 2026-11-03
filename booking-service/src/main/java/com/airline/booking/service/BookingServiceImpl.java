package com.airline.booking.service;

import com.airline.booking.domain.BookingStatus;
import com.airline.booking.domain.FlightStatus;
import com.airline.booking.dto.BookingDtos.*;
import com.airline.booking.entity.Booking;
import com.airline.booking.entity.BookingPassenger;
import com.airline.booking.entity.SeatAllocation;
import com.airline.booking.repository.BookingRepository;
import com.airline.booking.repository.SeatAllocationRepository;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
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
  private static final String PNR_ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
  private final SecureRandom random = new SecureRandom();
  private final BookingRepository bookings;
  private final SeatAllocationRepository seats;
  private final RestClient flightClient;

  public BookingServiceImpl(BookingRepository bookings, SeatAllocationRepository seats,
                            @Qualifier("flightRestClientBuilder") RestClient.Builder builder) {
    this.bookings = bookings;
    this.seats = seats;
    this.flightClient = builder.baseUrl("http://flight-service").build();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Integer> occupied(Long flightId) {
    if (flightId == null || flightId <= 0) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Flight ID must be positive");
    }
    return seats.findByFlightId(flightId).stream()
        .map(allocation -> allocation.seatNumber).sorted().toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<BookingResponse> mine(Long userId) {
    return bookings.findByUserIdOrderByBookedAtDesc(userId).stream().map(this::toResponse).toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<AdminBookingResponse> adminBookings(String flightNumber, BookingStatus status) {
    String searchNumber = flightNumber == null ? "" : flightNumber.trim().toUpperCase(Locale.ROOT);
    return bookings.findAllByOrderByBookedAtDesc().stream()
        .filter(b -> searchNumber.isEmpty() || b.flightNumber != null
            && b.flightNumber.toUpperCase(Locale.ROOT).equals(searchNumber))
        .filter(b -> status == null || b.status == status)
        .map(this::toAdminResponse).toList();
  }

  @Override
  @Transactional
  public BookingResponse book(Long userId, BookingRequest request) {
    FlightSnapshot flight = lookupFlight(request.flightId());
    if (flight == null || flight.status() != FlightStatus.OPEN
        || !"SCHEDULED".equals(flight.operationalStatus()) && !"DELAYED".equals(flight.operationalStatus())
        || !flight.departureTime().isAfter(LocalDateTime.now(Clock.systemUTC()))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Flight is not available for booking");
    }

    Set<Integer> requestedSeats = new HashSet<>();
    for (PassengerInput passenger : request.passengers()) {
      if (passenger.seatNumber() <= 0 || passenger.seatNumber() > flight.capacity()) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Seat outside aircraft capacity");
      }
      if (!requestedSeats.add(passenger.seatNumber())) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each passenger needs a different seat");
      }
    }

    BigDecimal total = flight.price().multiply(BigDecimal.valueOf(request.passengers().size()));
    Booking booking = new Booking(
        userId, flight.id(), request.passengers().get(0).seatNumber(), total, uniquePnr(),
        request.contactEmail().trim(), request.contactPhone(), flight.flightNumber(),
        flight.origin(), flight.destination(), flight.originCode(), flight.destinationCode(),
        flight.departureTime(), flight.arrivalTime());

    for (PassengerInput passenger : request.passengers()) {
      booking.addPassenger(new BookingPassenger(
          passenger.fullName().trim(), passenger.age(), passenger.seatNumber(), flight.price()));
    }

    Booking saved = bookings.saveAndFlush(booking);
    try {
      for (PassengerInput passenger : request.passengers()) {
        // Database UNIQUE(flight_id, seat_number) wins even if two browsers race.
        seats.saveAndFlush(new SeatAllocation(saved.id, flight.id(), passenger.seatNumber()));
      }
    } catch (DataIntegrityViolationException e) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "A selected seat was just booked");
    }
    return toResponse(saved);
  }

  @Override
  @Transactional
  public BookingResponse cancel(Long userId, Long id) {
    Booking booking = bookings.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
    if (!booking.userId.equals(userId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Booking belongs to another customer");
    }
    if (booking.status != BookingStatus.CONFIRMED) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Booking already cancelled");
    }
    FlightSnapshot flight = lookupFlight(booking.flightId);
    if (!flight.departureTime().isAfter(LocalDateTime.now(Clock.systemUTC()))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Past flights cannot be cancelled");
    }
    booking.status = BookingStatus.CANCELLED;
    seats.deleteByBookingId(booking.id);
    return toResponse(bookings.save(booking));
  }

  private String uniquePnr() {
    for (int retry = 0; retry < 10; retry++) {
      StringBuilder value = new StringBuilder(8);
      for (int i = 0; i < 8; i++) value.append(PNR_ALPHABET.charAt(random.nextInt(PNR_ALPHABET.length())));
      if (!bookings.existsByPnr(value.toString())) return value.toString();
    }
    throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Could not issue booking reference");
  }

  private BookingResponse toResponse(Booking b) {
    List<PassengerResponse> passengers = b.passengers.stream()
        .map(p -> new PassengerResponse(p.id, p.fullName, p.age, p.seatNumber, seatCode(p.seatNumber), p.fare))
        .toList();
    return new BookingResponse(b.id, b.pnr, b.flightId, b.seatNumber, b.totalFare, b.status,
        b.bookedAt, b.contactEmail, b.flightNumber, b.origin, b.destination, b.originCode,
        b.destinationCode, b.departureTime, b.arrivalTime, passengers);
  }

  private AdminBookingResponse toAdminResponse(Booking b) {
    return new AdminBookingResponse(b.id, b.pnr, b.userId, b.flightId, b.totalFare,
        b.status, b.bookedAt, b.passengers.size(), b.flightNumber, b.contactEmail);
  }

  private String seatCode(int n) {
    if (n < 1) return "?";
    int row = (n - 1) / 6 + 1;
    char letter = "ABCDEF".charAt((n - 1) % 6);
    return row + String.valueOf(letter);
  }

  private FlightSnapshot lookupFlight(Long flightId) {
    try {
      return flightClient.get().uri("/api/flights/{id}", flightId).retrieve().body(FlightSnapshot.class);
    } catch (HttpClientErrorException.NotFound e) {
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found");
    } catch (RestClientException e) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Flight service unavailable");
    }
  }
}

