package com.example.order.order_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.converter.JacksonJsonMessageConverter;

@Configuration
public class KafkaConfig {

    @Bean
    public JacksonJsonMessageConverter kafkaJsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }
}