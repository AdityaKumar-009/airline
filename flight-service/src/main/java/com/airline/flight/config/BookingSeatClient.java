package com.airline.flight.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Configuration
class BookingClientConfig {
  // Eureka must use the plain builder when discovering the registry itself.
  @Bean @Primary
  RestClient.Builder restClientBuilder() { return RestClient.builder(); }

  @Bean("bookingRestClientBuilder") @LoadBalanced
  RestClient.Builder bookingRestClientBuilder() {
    SimpleClientHttpRequestFactory factory=new SimpleClientHttpRequestFactory();
    factory.setConnectTimeout(Duration.ofSeconds(2));
    factory.setReadTimeout(Duration.ofSeconds(3));
    return RestClient.builder().requestFactory(factory);
  }
}

@Component
public class BookingSeatClient {
  private final RestClient client;
  public BookingSeatClient(@Qualifier("bookingRestClientBuilder") RestClient.Builder builder) {
    client=builder.baseUrl("http://booking-service").build();
  }
  public boolean hasActiveSeats(Long flightId) {
    try {
      Integer[] values=client.get().uri("/api/bookings/seats?flightId={id}",flightId)
        .retrieve().body(Integer[].class);
      return values!=null && values.length>0;
    } catch(RestClientException e) {
      throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
          "Booking service must be available to edit the flight schedule");
    }
  }
}

