package com.airline.flight;
import com.airline.flight.config.BookingSeatClient;
import com.airline.flight.dto.FlightDtos.CreateFlight;
import com.airline.flight.repository.*;
import com.airline.flight.service.FlightServiceImpl;
import java.math.BigDecimal;
import java.time.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
class FlightServiceTest {
  private final FlightServiceImpl service=new FlightServiceImpl(
    mock(FlightRepository.class),mock(AirportRepository.class),
    mock(FlightActivityRepository.class),mock(BookingSeatClient.class));
  @Test void rejectsPastDeparture(){
    LocalDateTime departure=LocalDateTime.now(Clock.systemUTC()).minusHours(1);
    var r=new CreateFlight("AI123","DEL","BOM",departure,departure.plusHours(2),
      BigDecimal.valueOf(5000),30,"A320");
    assertThrows(ResponseStatusException.class,()->service.create(r));
  }
}

