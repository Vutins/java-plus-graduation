package ru.practicum.explore_with_me.request;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.practicum.explore_with_me")
public class RequestServiceStarter {
    public static void main(String[] args) {
        SpringApplication.run(RequestServiceStarter.class, args);
    }
}