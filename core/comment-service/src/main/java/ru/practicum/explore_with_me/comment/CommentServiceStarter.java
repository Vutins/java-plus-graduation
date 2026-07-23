package ru.practicum.explore_with_me.comment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = "ru.practicum.explore_with_me")
public class CommentServiceStarter {
    public static void main(String[] args) {
        SpringApplication.run(CommentServiceStarter.class, args);
    }
}