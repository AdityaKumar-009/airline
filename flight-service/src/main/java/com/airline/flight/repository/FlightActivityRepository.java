package com.airline.flight.repository;
import com.airline.flight.entity.FlightActivity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface FlightActivityRepository extends JpaRepository<FlightActivity,Long> {
  List<FlightActivity> findTop50ByOrderByCreatedAtDesc();
}

