package ru.practicum;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.practicum")
public class CompilationStarter {
    public static void main(String[] args) {
        SpringApplication.run(CompilationStarter.class, args);
    }
}
