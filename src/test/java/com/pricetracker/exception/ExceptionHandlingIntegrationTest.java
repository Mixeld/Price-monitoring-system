// exception/ExceptionHandlingIntegrationTest.java
package com.pricetracker.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
@ContextConfiguration(classes = {ExceptionHandlingIntegrationTest.TestConfig.class})
class ExceptionHandlingIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Configuration
  static class TestConfig {

    @Bean
    public TestController testController() {
      return new TestController();
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
      return new GlobalExceptionHandler();
    }
  }

  @RestController
  static class TestController {

    @GetMapping("/not-found")
    public void throwNotFound() {
      throw new ResourceNotFoundException("User", "id", 999L);
    }

    @GetMapping("/duplicate")
    public void throwDuplicate() {
      throw new DuplicateResourceException("Email", "email", "test@test.com");
    }

    @GetMapping("/business")
    public void throwBusiness() {
      throw new BusinessException("Business error", "BIZ_001");
    }

    @GetMapping("/cannot-delete")
    public void throwCannotDelete() {
      throw new CannotDeleteException("Cannot delete resource with dependencies");
    }

    @GetMapping("/illegal-argument")
    public void throwIllegalArgument() {
      throw new IllegalArgumentException("Invalid argument provided");
    }
  }

  @Test
  void shouldReturn404ForResourceNotFound() throws Exception {
    mockMvc.perform(get("/not-found"))
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.error").value("Not Found"))
        .andExpect(jsonPath("$.message").value("User not found with id: '999'"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void shouldReturn409ForDuplicateResource() throws Exception {
    mockMvc.perform(get("/duplicate"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.status").value(409))
        .andExpect(jsonPath("$.error").value("Conflict"))
        .andExpect(jsonPath("$.message").value("Email with email 'test@test.com' already exists"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void shouldReturn422ForBusinessException() throws Exception {
    mockMvc.perform(get("/business"))
        .andExpect(status().isUnprocessableEntity())
        .andExpect(jsonPath("$.status").value(422))
        .andExpect(jsonPath("$.error").value("Business Error"))
        .andExpect(jsonPath("$.message").value("Business error"))
        .andExpect(jsonPath("$.errorCode").value("BIZ_001"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void shouldReturn400ForCannotDeleteException() throws Exception {
    mockMvc.perform(get("/cannot-delete"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Cannot delete resource with dependencies"))
        .andExpect(jsonPath("$.timestamp").exists());
  }

  @Test
  void shouldReturn400ForIllegalArgumentException() throws Exception {
    mockMvc.perform(get("/illegal-argument"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.status").value(400))
        .andExpect(jsonPath("$.error").value("Bad Request"))
        .andExpect(jsonPath("$.message").value("Invalid argument provided"))
        .andExpect(jsonPath("$.timestamp").exists());
  }
}