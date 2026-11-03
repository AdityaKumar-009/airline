package com.airline.flight;

import com.airline.flight.config.BookingSeatClient;
import com.airline.flight.dto.FlightDtos.CreateFlight;
import com.airline.flight.entity.Airport;
import com.airline.flight.repository.*;
import com.airline.flight.service.FlightServiceImpl;
import java.math.BigDecimal;
import java.time.*;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class FlightServiceTest {
  private final FlightRepository flights = mock(FlightRepository.class);
  private final AirportRepository airports = mock(AirportRepository.class);
  private final FlightServiceImpl service = new FlightServiceImpl(
      flights, airports, mock(FlightActivityRepository.class), mock(BookingSeatClient.class));

  @Test
  void rejectsPastDeparture() {
    LocalDateTime departure = LocalDateTime.now(Clock.systemUTC()).minusHours(1);
    var request = new CreateFlight("AI123", "DEL", "BOM", departure,
        departure.plusHours(2), BigDecimal.valueOf(5000), 30, "A320");
    assertThrows(ResponseStatusException.class, () -> service.create(request));
    verifyNoInteractions(flights);
  }

  @Test
  void rejectsSameOriginAndDestinationAirport() {
    LocalDateTime departure = LocalDateTime.now(Clock.systemUTC()).plusDays(2);
    Airport delhi = mock(Airport.class);
    delhi.code = "DEL";
    delhi.city = "Delhi";
    when(airports.findById("DEL")).thenReturn(Optional.of(delhi));
    var request = new CreateFlight("AI123", "DEL", "DEL", departure,
        departure.plusHours(2), BigDecimal.valueOf(5000), 30, "A320");
    assertThrows(ResponseStatusException.class, () -> service.create(request));
    verifyNoInteractions(flights);
  }
}
