package com.airline.auth;

import com.airline.auth.controller.ApiErrors;
import com.airline.auth.controller.AuthController;
import com.airline.auth.service.AuthService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthValidationTest {
  private final AuthService service = mock(AuthService.class);
  private final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
  private MockMvc mvc;

  @BeforeEach
  void setUp() {
    validator.afterPropertiesSet();
    mvc = MockMvcBuilders.standaloneSetup(new AuthController(service))
        .setControllerAdvice(new ApiErrors()).setValidator(validator).build();
  }

  @AfterEach
  void closeValidator() { validator.close(); }

  @Test
  void registrationRejectsNamesWithNumbers() throws Exception {
    mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Casey 2\",\"email\":\"casey@example.com\",\"password\":\"Password123\"}"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(service);
  }

  @Test
  void registrationRejectsEmailWithoutValidTopLevelDomain() throws Exception {
    mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Casey Customer\",\"email\":\"casey@example.c\",\"password\":\"Password123\"}"))
        .andExpect(status().isBadRequest());
    verifyNoInteractions(service);
  }

  @Test
  void registrationAcceptsAValidFullNameAndEmailWithSurroundingNameSpaces() throws Exception {
    mvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"  Casey Customer  \",\"email\":\"casey@example.com\",\"password\":\"Password123\"}"))
        .andExpect(status().isCreated());
    org.mockito.Mockito.verify(service).register(org.mockito.ArgumentMatchers.any());
  }
}
