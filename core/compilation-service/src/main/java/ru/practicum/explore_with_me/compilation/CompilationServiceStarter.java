package ru.practicum.explore_with_me.compilation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.practicum.explore_with_me")
public class CompilationServiceStarter {
    public static void main(String[] args) {
        SpringApplication.run(CompilationServiceStarter.class, args);
    }
}