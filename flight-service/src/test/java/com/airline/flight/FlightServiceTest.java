package com.airline.flight;
import com.airline.flight.dto.FlightDtos.CreateFlight;
import com.airline.flight.repository.FlightRepository;
import com.airline.flight.service.FlightServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class FlightServiceTest {
  private final FlightRepository repo=mock(FlightRepository.class);
  private final FlightServiceImpl flights=new FlightServiceImpl(repo);
  @Test void rejectsSameOriginAndDestination(){
    LocalDateTime departure=LocalDateTime.now(Clock.systemUTC()).plusDays(2);
    var request=new CreateFlight("AI123","Delhi","DELHI",departure,departure.plusHours(2),BigDecimal.valueOf(5000),30);
    assertThrows(ResponseStatusException.class,()->flights.create(request));
    verifyNoInteractions(repo);
  }
  @Test void rejectsDepartureInPast(){
    LocalDateTime departure=LocalDateTime.now(Clock.systemUTC()).minusHours(1);
    var request=new CreateFlight("AI123","Delhi","Mumbai",departure,departure.plusHours(2),BigDecimal.valueOf(5000),30);
    assertEquals(400,assertThrows(ResponseStatusException.class,()->flights.create(request)).getStatusCode().value());
  }
}
