package ru.practicum.explore_with_me.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.aggregator.consumer.UserActionConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregatorRunner implements CommandLineRunner {

    private final UserActionConsumer userActionConsumer;

    @Override
    public void run(String... args) throws Exception {
        userActionConsumer.start();
    }
}