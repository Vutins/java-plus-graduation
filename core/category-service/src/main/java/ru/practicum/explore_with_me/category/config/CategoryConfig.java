package ru.practicum.explore_with_me.category.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;
import ru.practicum.explore_with_me.StatsClient;

import java.time.Duration;

@Configuration
public class CategoryConfig {

    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    public StatsClient statsClient(@LoadBalanced RestClient.Builder restClientBuilder) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout((int) Duration.ofSeconds(5).toMillis());
        requestFactory.setReadTimeout((int) Duration.ofSeconds(5).toMillis());

        RestClient restClient = restClientBuilder
                .baseUrl("http://stats-server")
                .requestFactory(requestFactory)
                .build();

        return new StatsClient(restClient);
    }
}