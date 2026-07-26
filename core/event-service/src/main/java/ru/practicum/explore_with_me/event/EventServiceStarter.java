package ru.practicum.explore_with_me.event;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.practicum.explore_with_me")
public class EventServiceStarter {
    public static void main(String[] args) {
        SpringApplication.run(EventServiceStarter.class, args);
    }
}