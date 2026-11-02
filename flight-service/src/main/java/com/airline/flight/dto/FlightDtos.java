package com.airline.flight.dto;

import com.airline.flight.domain.FlightStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;
public class FlightDtos {
  public record CreateFlight(@NotBlank String flightNumber,@NotBlank String origin,@NotBlank String destination,
    @NotNull LocalDateTime departureTime,@NotNull LocalDateTime arrivalTime,
    @NotNull @DecimalMin("0.01") BigDecimal price,@Min(1) @Max(300) int capacity) {}
  public record EditFlight(@DecimalMin("0.01") BigDecimal price,FlightStatus status) {}
  public record FlightResponse(Long id,String flightNumber,String origin,String destination,LocalDateTime departureTime,
    LocalDateTime arrivalTime,BigDecimal price,int capacity,FlightStatus status) {}
}
