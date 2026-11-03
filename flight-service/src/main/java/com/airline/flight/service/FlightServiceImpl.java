package com.airline.flight.service;

import com.airline.flight.config.BookingSeatClient;
import com.airline.flight.domain.*;
import com.airline.flight.dto.FlightDtos.*;
import com.airline.flight.entity.*;
import com.airline.flight.repository.*;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FlightServiceImpl implements FlightService {
  private final FlightRepository flights;
  private final AirportRepository airports;
  private final FlightActivityRepository activities;
  private final BookingSeatClient seats;

  public FlightServiceImpl(FlightRepository flights,AirportRepository airports,
                           FlightActivityRepository activities,BookingSeatClient seats) {
    this.flights=flights;this.airports=airports;this.activities=activities;this.seats=seats;
  }

  @Override public List<FlightResponse> search(String origin,String destination,LocalDate date) {
    LocalDateTime now=LocalDateTime.now(Clock.systemUTC());
    return flights.findByStatusOrderByDepartureTimeAsc(FlightStatus.OPEN).stream()
      .filter(f -> f.departureTime.isAfter(now))
      .filter(f -> f.operationalStatus==OperationalStatus.SCHEDULED || f.operationalStatus==OperationalStatus.DELAYED)
      .filter(f -> matches(origin,f.origin,f.originCode))
      .filter(f -> matches(destination,f.destination,f.destinationCode))
      .filter(f -> date==null || f.departureTime.toLocalDate().equals(date))
      .map(this::toResponse).toList();
  }
  private boolean matches(String search,String city,String code) {
    if(search==null || search.isBlank()) return true;
    String s=search.trim();
    return city.equalsIgnoreCase(s) || code!=null && code.equalsIgnoreCase(s);
  }
  @Override public List<FlightResponse> manage() {
    return flights.findAll().stream().sorted(Comparator.comparing(f -> f.departureTime))
      .map(this::toResponse).toList();
  }
  @Override public FlightResponse get(Long id) {return toResponse(find(id));}

  @Override @Transactional
  public FlightResponse create(CreateFlight request) {
    validateSchedule(request.departureTime(),request.arrivalTime());
    Airport from=airport(request.originCode());
    Airport to=airport(request.destinationCode());
    if(from.code.equals(to.code))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Origin and destination must differ");
    Flight flight=new Flight(request.flightNumber().trim().toUpperCase(),from.city,to.city,
      request.departureTime(),request.arrivalTime(),request.price(),request.capacity());
    flight.originCode=from.code;flight.destinationCode=to.code;
    flight.aircraftType=request.aircraftType().trim();
    flight=flights.saveAndFlush(flight);
    activities.save(new FlightActivity(flight.id,"CREATED","Created flight "+flight.flightNumber));
    return toResponse(flight);
  }

  @Override @Transactional
  public FlightResponse edit(Long id,EditFlight changes) {
    Flight f=find(id);
    if(changes.departureTime()!=null || changes.arrivalTime()!=null) {
      if(f.operationalStatus==OperationalStatus.CANCELLED
        || f.operationalStatus==OperationalStatus.DEPARTED
        || f.operationalStatus==OperationalStatus.COMPLETED)
        throw new ResponseStatusException(HttpStatus.CONFLICT,"Flight schedule can no longer change");
      if(seats.hasActiveSeats(id))
        throw new ResponseStatusException(HttpStatus.CONFLICT,
          "Cancel active reservations before rescheduling this flight");
      LocalDateTime departure=changes.departureTime()==null?f.departureTime:changes.departureTime();
      LocalDateTime arrival=changes.arrivalTime()==null?f.arrivalTime:changes.arrivalTime();
      validateSchedule(departure,arrival);
      f.departureTime=departure;f.arrivalTime=arrival;
    }
    if(changes.operationalStatus()!=null) {
      if(f.operationalStatus==OperationalStatus.CANCELLED
        && changes.operationalStatus()!=OperationalStatus.CANCELLED)
        throw new ResponseStatusException(HttpStatus.CONFLICT,"Cancelled flights cannot be reopened");
      f.operationalStatus=changes.operationalStatus();
      if(f.operationalStatus==OperationalStatus.CANCELLED
        || f.operationalStatus==OperationalStatus.DEPARTED
        || f.operationalStatus==OperationalStatus.COMPLETED) f.status=FlightStatus.CLOSED;
    }
    if(changes.status()!=null) {
      if(changes.status()==FlightStatus.OPEN
        && (f.operationalStatus==OperationalStatus.CANCELLED
          || f.operationalStatus==OperationalStatus.DEPARTED
          || f.operationalStatus==OperationalStatus.COMPLETED
          || !f.departureTime.isAfter(LocalDateTime.now(Clock.systemUTC()))))
        throw new ResponseStatusException(HttpStatus.CONFLICT,"Flight cannot be reopened");
      f.status=changes.status();
    }
    if(changes.price()!=null) f.price=changes.price();
    if(changes.aircraftType()!=null) {
      if(changes.aircraftType().isBlank())
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Aircraft type is required");
      f.aircraftType=changes.aircraftType().trim();
    }
    f=flights.saveAndFlush(f);
    activities.save(new FlightActivity(f.id,"UPDATED","Sales: "+f.status+", operation: "+f.operationalStatus));
    return toResponse(f);
  }

  @Override public List<AirportResponse> airports() {
    return airports.findAll().stream().sorted(Comparator.comparing(a -> a.city))
      .map(a -> new AirportResponse(a.code,a.name,a.city)).toList();
  }
  @Override public List<FlightActivityResponse> activity() {
    return activities.findTop50ByOrderByCreatedAtDesc().stream()
      .map(a -> new FlightActivityResponse(a.id,a.flightId,a.action,a.description,a.createdAt)).toList();
  }
  private Airport airport(String code) {
    if(code==null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Airport code required");
    return airports.findById(code.trim().toUpperCase(java.util.Locale.ROOT))
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,"Unknown airport "+code));
  }
  private void validateSchedule(LocalDateTime departure,LocalDateTime arrival) {
    if(!departure.isAfter(LocalDateTime.now(Clock.systemUTC())))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Departure must be in the future (UTC)");
    if(!arrival.isAfter(departure))
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Arrival must follow departure");
  }
  private Flight find(Long id) {
    return flights.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Flight not found"));
  }
  private FlightResponse toResponse(Flight f) {
    return new FlightResponse(f.id,f.flightNumber,f.origin,f.destination,f.departureTime,
      f.arrivalTime,f.price,f.capacity,f.status,f.originCode,f.destinationCode,
      f.aircraftType,f.operationalStatus);
  }
}

