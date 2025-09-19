package com.school.scheduling.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("School Scheduling System API")
                        .version("v1.0.0")
                        .description("A comprehensive REST API for managing university course scheduling system")
                        .contact(new Contact()
                                .name("School Scheduling Team")
                                .email("support@school.edu")
                                .url("https://school.edu/scheduling"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(Arrays.asList(
                        new Server()
                                .url("/api")
                                .description("Production Server"),
                        new Server()
                                .url("http://localhost:8080/api")
                                .description("Development Server")
                ));
    }
}