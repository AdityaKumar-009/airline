package com.airline.booking.repository;
import com.airline.booking.entity.SeatAllocation;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SeatAllocationRepository extends JpaRepository<SeatAllocation,Long>{
  List<SeatAllocation> findByFlightId(Long flightId);
}
