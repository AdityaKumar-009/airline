package com.airline.booking.config;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class FlightClientConfig {
  @Bean
  @Primary
  RestClient.Builder restClientBuilder() {
    return builderWithTimeouts();
  }

  @Bean
  @LoadBalanced
  @Qualifier("flightRestClientBuilder")
  RestClient.Builder flightRestClientBuilder() {
    // The load balancer lets the booking service call the flight service by its Eureka name.
    return builderWithTimeouts();
  }

  private RestClient.Builder builderWithTimeouts() {
    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    requestFactory.setConnectTimeout(Duration.ofSeconds(2));
    requestFactory.setReadTimeout(Duration.ofSeconds(3));
    return RestClient.builder().requestFactory(requestFactory);
  }
}
