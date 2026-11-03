package com.airline.flight.dto;
import com.airline.flight.domain.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.*;
public class FlightDtos {
  private static final String LETTERS_AND_NUMBERS = "^[A-Za-z0-9]+$";

  public record CreateFlight(@NotBlank @Pattern(regexp=LETTERS_AND_NUMBERS) @Size(max=20) String flightNumber,
    @NotBlank @Pattern(regexp="^[A-Za-z]{3}$") String originCode,
    @NotBlank @Pattern(regexp="^[A-Za-z]{3}$") String destinationCode,
    @NotNull LocalDateTime departureTime,@NotNull LocalDateTime arrivalTime,
    @NotNull @DecimalMin("0.01") BigDecimal price,@Min(1) @Max(300) int capacity,
    @NotBlank @Pattern(regexp=LETTERS_AND_NUMBERS) @Size(max=40) String aircraftType){}
  public record EditFlight(@DecimalMin("0.01") BigDecimal price,FlightStatus status,
    LocalDateTime departureTime,LocalDateTime arrivalTime,OperationalStatus operationalStatus,
    @Pattern(regexp=LETTERS_AND_NUMBERS) @Size(max=40) String aircraftType){}
  public record FlightResponse(Long id,String flightNumber,String origin,String destination,
    LocalDateTime departureTime,LocalDateTime arrivalTime,BigDecimal price,int capacity,
    FlightStatus status,String originCode,String destinationCode,String aircraftType,
    OperationalStatus operationalStatus){}
  public record AirportResponse(String code,String name,String city){}
  public record FlightActivityResponse(Long id,Long flightId,String action,
    String description,LocalDateTime createdAt){}
}

