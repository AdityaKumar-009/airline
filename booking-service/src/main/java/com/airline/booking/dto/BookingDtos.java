package com.airline.booking.dto;

import com.airline.booking.domain.BookingStatus;
import com.airline.booking.domain.FlightStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BookingDtos {
  public record PassengerInput(
      @NotBlank @Size(max = 100) String fullName,
      @Min(1) @Max(120) int age,
      @Min(1) int seatNumber) {}

  public record BookingRequest(
      @NotNull @Positive Long flightId,
      @NotBlank @Email @Size(max = 150) String contactEmail,
      @Pattern(regexp = "^$|^\\+?[0-9]{10,15}$") String contactPhone,
      // Collection-level cascading avoids duplicate type annotations in older Eclipse compilers.
      @NotEmpty @Size(max = 4) @Valid List<PassengerInput> passengers) {}

  public record PassengerResponse(Long id, String fullName, int age, int seatNumber,
                                  String seatCode, BigDecimal fare) {}

  public record BookingResponse(
      Long id, String pnr, Long flightId, int seatNumber, BigDecimal totalFare,
      BookingStatus status, LocalDateTime bookedAt, String contactEmail,
      String flightNumber, String origin, String destination,
      String originCode, String destinationCode, LocalDateTime departureTime,
      LocalDateTime arrivalTime, List<PassengerResponse> passengers) {}

  public record AdminBookingResponse(
      Long id, String pnr, Long userId, Long flightId, BigDecimal totalFare,
      BookingStatus status, LocalDateTime bookedAt, int passengerCount,
      String flightNumber, String contactEmail) {}

  public record FlightSnapshot(
      Long id, String flightNumber, String origin, String destination,
      LocalDateTime departureTime, LocalDateTime arrivalTime,
      BigDecimal price, int capacity, FlightStatus status,
      String originCode, String destinationCode, String aircraftType,
      String operationalStatus) {}
}

