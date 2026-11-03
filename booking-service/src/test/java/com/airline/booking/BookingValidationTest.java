package com.airline.booking;

import com.airline.booking.controller.ApiErrors;
import com.airline.booking.controller.BookingController;
import com.airline.booking.dto.BookingDtos.BookingRequest;
import com.airline.booking.service.BookingService;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookingValidationTest {
  private final BookingService service = mock(BookingService.class);
  private final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
  private final JwtAuthenticationToken customer = new JwtAuthenticationToken(
      Jwt.withTokenValue("test-token").header("alg", "RS256").subject("7").build());
  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    validator.afterPropertiesSet();
    mvc = MockMvcBuilders.standaloneSetup(new BookingController(service))
        .setControllerAdvice(new ApiErrors()).setValidator(validator).build();
  }

  @AfterEach
  void closeValidator() {
    validator.close();
  }

  @ParameterizedTest
  @ValueSource(ints = {1, 4})
  void validPassengersReachBookingService(int count) throws Exception {
    String passengers = Stream.iterate(1, seat -> seat + 1).limit(count)
        .map(seat -> passenger("Passenger " + seat, seat == 1 ? 1 : 120, seat))
        .collect(java.util.stream.Collectors.joining(","));

    mvc.perform(post("/api/bookings").principal(customer)
            .contentType(MediaType.APPLICATION_JSON).content(request(passengers)))
        .andExpect(status().isCreated());

    var booking = ArgumentCaptor.forClass(BookingRequest.class);
    verify(service).book(eq(7L), booking.capture());
    assertEquals(count, booking.getValue().passengers().size());
    assertEquals("Passenger 1", booking.getValue().passengers().get(0).fullName());
    assertEquals(1L, booking.getValue().flightId());
  }

  @ParameterizedTest(name = "Invalid passenger {index}: {1}")
  @MethodSource("invalidPassengers")
  void invalidNestedPassengerReturns400(String invalidPassenger, String field) throws Exception {
    // Put the invalid passenger second to check that every list element is validated.
    String body = request(passenger("Valid Passenger", 25, 1) + "," + invalidPassenger);
    mvc.perform(post("/api/bookings").principal(customer)
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("passengers[1]." + field)));
    verifyNoInteractions(service);
  }

  static Stream<Arguments> invalidPassengers() {
    return Stream.of(
        Arguments.of("{\"age\":25,\"seatNumber\":2}", "fullName"),
        Arguments.of(passenger(" ", 25, 2), "fullName"),
        Arguments.of(passenger("A".repeat(101), 25, 2), "fullName"),
        Arguments.of(passenger("Passenger", 0, 2), "age"),
        Arguments.of(passenger("Passenger", 121, 2), "age"),
        Arguments.of(passenger("Passenger", 25, 0), "seatNumber"));
  }

  @ParameterizedTest(name = "Invalid booking {index}: {1}")
  @MethodSource("invalidBookings")
  void invalidBookingReturns400(String body, String field) throws Exception {
    mvc.perform(post("/api/bookings").principal(customer)
            .contentType(MediaType.APPLICATION_JSON).content(body))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString(field)));
    verifyNoInteractions(service);
  }

  static Stream<Arguments> invalidBookings() {
    String valid = request(passenger("Passenger", 25, 1));
    String fivePassengers = Stream.iterate(1, seat -> seat + 1).limit(5)
        .map(seat -> passenger("Passenger", 25, seat))
        .collect(java.util.stream.Collectors.joining(","));
    return Stream.of(
        Arguments.of(request(""), "passengers"),
        Arguments.of(request("").replace("[]", "null"), "passengers"),
        Arguments.of(request(fivePassengers), "passengers"),
        Arguments.of(valid.replace("\"flightId\":1", "\"flightId\":null"), "flightId"),
        Arguments.of(valid.replace("\"flightId\":1", "\"flightId\":0"), "flightId"),
        Arguments.of(valid.replace("passenger@example.test", ""), "contactEmail"),
        Arguments.of(valid.replace("passenger@example.test", "invalid-email"), "contactEmail"),
        Arguments.of(valid.replace("\"contactPhone\":\"\"", "\"contactPhone\":\"123\""), "contactPhone"));
  }

  private static String passenger(String name, int age, int seat) {
    return "{\"fullName\":\"%s\",\"age\":%d,\"seatNumber\":%d}".formatted(name, age, seat);
  }

  private static String request(String passengers) {
    return """
        {"flightId":1,"contactEmail":"passenger@example.test",
         "contactPhone":"","passengers":[%s]}
        """.formatted(passengers);
  }
}
