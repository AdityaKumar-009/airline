package com.airline.flight.repository;
import com.airline.flight.entity.Airport;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AirportRepository extends JpaRepository<Airport,String> {}

