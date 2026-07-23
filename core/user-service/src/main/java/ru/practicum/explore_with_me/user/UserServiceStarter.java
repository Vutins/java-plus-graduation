package ru.practicum.explore_with_me.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.practicum.explore_with_me")
public class UserServiceStarter {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceStarter.class, args);
    }
}
