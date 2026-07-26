package ru.practicum.explore_with_me.analyzer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.practicum.explore_with_me.analyzer.processor.actions.UserActionsProcessor;
import ru.practicum.explore_with_me.analyzer.processor.similarities.EventSimilaritiesProcessor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerRunner implements CommandLineRunner {

    private final UserActionsProcessor userActionsProcessor;
    private final EventSimilaritiesProcessor similaritiesProcessor;

    @Override
    public void run(String... args) throws Exception {
        Thread userActionThread = new Thread(userActionsProcessor);
        userActionThread.setName("UserActionsProcessorThread");
        userActionThread.start();

        Thread eventSimThread = new Thread(similaritiesProcessor);
        eventSimThread.setName("EventSimilarityProcessorThread");
        eventSimThread.start();
    }
}