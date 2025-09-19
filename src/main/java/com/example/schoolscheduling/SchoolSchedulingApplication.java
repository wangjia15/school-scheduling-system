package com.example.schoolscheduling;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableMethodSecurity
public class SchoolSchedulingApplication {
    public static void main(String[] args) {
        SpringApplication.run(SchoolSchedulingApplication.class, args);
    }
}