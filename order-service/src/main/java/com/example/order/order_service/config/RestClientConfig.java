package com.example.order.order_service.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient productRestClient(
            @Value("${service.product.url}") String productServiceUrl
    ) {

        return RestClient.builder()
                .baseUrl(productServiceUrl)
                .requestInterceptor((request, body, execution) -> {

                    System.out.println("요청 URL = " + request.getURI());
                    System.out.println("요청 Method = " + request.getMethod());

                    return execution.execute(request, body);
                })
                .build();
    }
}