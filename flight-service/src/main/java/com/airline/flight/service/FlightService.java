package com.airline.flight.service;
import com.airline.flight.dto.FlightDtos.*;
import java.time.LocalDate;
import java.util.List;
public interface FlightService {
  List<FlightResponse> search(String origin,String destination,LocalDate date);
  List<FlightResponse> manage();
  FlightResponse get(Long id);
  FlightResponse create(CreateFlight request);
  FlightResponse edit(Long id,EditFlight changes);
  List<AirportResponse> airports();
  List<FlightActivityResponse> activity();
}

