package com.airline.booking.dto;

import com.airline.booking.domain.BookingStatus;
import com.airline.booking.domain.FlightStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;
public class BookingDtos {
  public record BookingRequest(@NotNull Long flightId,@Min(1) int seatNumber) {}
  public record BookingResponse(Long id,Long flightId,int seatNumber,BigDecimal fareAtBooking,BookingStatus status,LocalDateTime bookedAt) {}
  public record AdminBookingResponse(Long id,Long userId,Long flightId,int seatNumber,BigDecimal fareAtBooking,BookingStatus status,LocalDateTime bookedAt) {}
  public record FlightSnapshot(Long id,String flightNumber,String origin,String destination,LocalDateTime departureTime,
    LocalDateTime arrivalTime,BigDecimal price,int capacity,FlightStatus status) {}
}
