package com.pricetracker;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .info(new Info()
            .title("Price Tracker API")
            .version("1.0")
            .description("API for tracking product prices across different stores")
            .contact(new Contact()
                .name("Price Tracker Team")
                .email("support@pricetracker.com"))
            .license(new License()
                .name("Apache 2.0")
                .url("http://springdoc.org")))
        .servers(List.of(
            new Server().url("http://localhost:8080").description("Development Server")
        ));
  }
}