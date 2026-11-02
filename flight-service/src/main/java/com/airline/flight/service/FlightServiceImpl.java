package com.airline.flight.service;

import com.airline.flight.domain.FlightStatus;
import com.airline.flight.dto.FlightDtos.CreateFlight;
import com.airline.flight.dto.FlightDtos.EditFlight;
import com.airline.flight.dto.FlightDtos.FlightResponse;
import com.airline.flight.entity.Flight;
import com.airline.flight.repository.FlightRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FlightServiceImpl implements FlightService {
  private final FlightRepository flights;

  public FlightServiceImpl(FlightRepository flights) {
    this.flights = flights;
  }

  @Override
  public List<FlightResponse> search(String origin, String destination, LocalDate date) {
    LocalDateTime nowUtc = LocalDateTime.now(Clock.systemUTC());
    List<FlightResponse> results = new ArrayList<>();

    for (Flight flight : flights.findByStatusOrderByDepartureTimeAsc(FlightStatus.OPEN)) {
      if (!flight.departureTime.isAfter(nowUtc)) continue;
      if (origin != null && !flight.origin.equalsIgnoreCase(origin.trim())) continue;
      if (destination != null && !flight.destination.equalsIgnoreCase(destination.trim())) continue;
      if (date != null && !flight.departureTime.toLocalDate().equals(date)) continue;
      results.add(toResponse(flight));
    }
    return results;
  }

  @Override
  public List<FlightResponse> manage() {
    return flights.findAll().stream()
        .sorted(Comparator.comparing(flight -> flight.departureTime))
        .map(this::toResponse)
        .toList();
  }

  @Override
  public FlightResponse get(Long id) {
    return toResponse(find(id));
  }

  @Override
  public FlightResponse create(CreateFlight request) {
    if (!request.departureTime().isAfter(LocalDateTime.now(Clock.systemUTC()))) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Departure must be in the future (UTC)");
    }
    if (request.origin().equalsIgnoreCase(request.destination())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Origin and destination must differ");
    }
    if (!request.arrivalTime().isAfter(request.departureTime())) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Arrival must be after departure");
    }

    Flight flight = new Flight(
        request.flightNumber().trim().toUpperCase(),
        request.origin().trim(),
        request.destination().trim(),
        request.departureTime(),
        request.arrivalTime(),
        request.price(),
        request.capacity());
    return toResponse(flights.save(flight));
  }

  @Override
  public FlightResponse edit(Long id, EditFlight changes) {
    Flight flight = find(id);
    if (changes.price() != null) flight.price = changes.price();
    if (changes.status() != null) flight.status = changes.status();
    return toResponse(flights.save(flight));
  }

  private Flight find(Long id) {
    return flights.findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Flight not found"));
  }

  private FlightResponse toResponse(Flight flight) {
    return new FlightResponse(
        flight.id,
        flight.flightNumber,
        flight.origin,
        flight.destination,
        flight.departureTime,
        flight.arrivalTime,
        flight.price,
        flight.capacity,
        flight.status);
  }
}
