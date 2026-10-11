package com.smartstudy.quizservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Jackson2Config {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}