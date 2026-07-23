package ru.practicum.explore_with_me.category;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.practicum.explore_with_me")
public class CategoryServiceStarter {
    public static void main(String[] args) {
        SpringApplication.run(CategoryServiceStarter.class, args);
    }
}