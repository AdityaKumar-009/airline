package com.airline.flight;

import com.airline.flight.controller.ApiErrors;
import com.airline.flight.controller.FlightController;
import com.airline.flight.service.FlightService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FlightValidationTest {
  private final FlightService service = mock(FlightService.class);
  private final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    validator.afterPropertiesSet();
    mvc = MockMvcBuilders.standaloneSetup(new FlightController(service))
        .setControllerAdvice(new ApiErrors()).setValidator(validator).build();
  }

  @AfterEach
  void closeValidator() { validator.close(); }

  @Test
  void createRejectsSymbolsInFlightNumberAndAircraftType() throws Exception {
    String valid = "\"originCode\":\"DEL\",\"destinationCode\":\"BOM\","
        + "\"departureTime\":\"2027-01-01T10:00:00\",\"arrivalTime\":\"2027-01-01T12:00:00\","
        + "\"price\":4500,\"capacity\":30";
    mvc.perform(post("/api/flights").contentType(MediaType.APPLICATION_JSON)
            .content("{" + valid + ",\"flightNumber\":\"AI-109\",\"aircraftType\":\"A320\"}"))
        .andExpect(status().isBadRequest());
    mvc.perform(post("/api/flights").contentType(MediaType.APPLICATION_JSON)
            .content("{" + valid + ",\"flightNumber\":\"AI109\",\"aircraftType\":\"A320!\"}"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(service);
  }

  @Test
  void editRejectsSymbolsInAircraftType() throws Exception {
    mvc.perform(patch("/api/flights/1").contentType(MediaType.APPLICATION_JSON)
            .content("{\"aircraftType\":\"A320!\"}"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(service);
  }
}
