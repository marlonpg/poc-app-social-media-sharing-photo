package com.gamba.software.photoapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@SpringBootApplication
public class PhotoServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PhotoServiceApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(5)) // Connection timeout
                .readTimeout(Duration.ofSeconds(5))    // Read timeout
                .build();
    }
}
