package com.fitness.aiservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler kafkaErrorHandler() {

        FixedBackOff backOff =
                new FixedBackOff(
                        20_000L,  // wait 20 seconds
                        3L        // retry 3 times
                );

        return new DefaultErrorHandler(backOff);
    }
}