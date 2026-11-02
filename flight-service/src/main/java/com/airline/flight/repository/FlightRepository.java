package com.airline.flight.repository;

import com.airline.flight.domain.FlightStatus;
import com.airline.flight.entity.Flight;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FlightRepository extends JpaRepository<Flight,Long> {
  List<Flight> findByStatusOrderByDepartureTimeAsc(FlightStatus status);
}
