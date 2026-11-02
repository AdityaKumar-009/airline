package com.airline.flight.service;

import com.airline.flight.entity.Flight;
import com.airline.flight.repository.FlightRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DemoFlights {
  @Bean
  CommandLineRunner seedFlights(FlightRepository flights) {
    return args -> {
      if (flights.count() > 0) {
        return;
      }

      String[][] routes = {
          {"AI101", "Delhi", "Mumbai", "4500"},
          {"6E202", "Delhi", "Bengaluru", "5200"},
          {"UK303", "Mumbai", "Delhi", "4800"},
          {"AI404", "Delhi", "Hyderabad", "3900"},
          {"6E505", "Bengaluru", "Chennai", "3100"},
          {"AI606", "Mumbai", "Goa", "2800"},
          {"UK707", "Delhi", "Kolkata", "4600"},
          {"6E808", "Hyderabad", "Delhi", "4200"}
      };

      LocalDate firstDepartureDay = LocalDate.now(Clock.systemUTC()).plusDays(3);
      for (int i = 0; i < routes.length; i++) {
        String[] route = routes[i];
        LocalDate flightDay = firstDepartureDay.plusDays(i / 3);
        int departureHour = 8 + (i % 3) * 4;
        LocalDateTime departure = flightDay.atTime(departureHour, 0);

        Flight flight = new Flight(
            route[0],
            route[1],
            route[2],
            departure,
            departure.plusHours(2),
            new BigDecimal(route[3]),
            30);
        flights.save(flight);
      }
    };
  }
}
