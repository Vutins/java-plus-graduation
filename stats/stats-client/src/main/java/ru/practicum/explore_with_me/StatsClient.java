package ru.practicum.explore_with_me;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.StringJoiner;

@Slf4j
public class StatsClient {
    private final RestClient restClient;

    public StatsClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public void hit(EndpointHitDto endpointHitDto) {
        try {
            restClient.post()
                    .uri("/hit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(endpointHitDto)
                    .retrieve()
                    .toBodilessEntity();
            log.info("Хит успешно отправлен");
        } catch (Exception e) {
            log.error("Ошибка при отправке хита в сервис статистики: {}", e.getMessage());
        }
    }

    public List<StatResponseDto> getStats(LocalDateTime start, LocalDateTime end,
                                          List<String> uris, Boolean unique) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        StringJoiner uriJoiner = new StringJoiner(",");
        if (uris != null && !uris.isEmpty()) {
            uris.forEach(uriJoiner::add);
        }

        String uri = String.format(
                "/stats?start=%s&end=%s&unique=%s&uris=%s",
                start.format(formatter),
                end.format(formatter),
                unique,
                uriJoiner.toString()
        );

        try {
            StatResponseDto[] response = restClient.get()
                    .uri(uri)
                    .retrieve()
                    .body(StatResponseDto[].class);
            log.info("Статистика успешно получена от сервиса");
            return List.of(response);

        } catch (Exception e) {
            log.error("Ошибка при получении статистики от сервиса: {}", e.getMessage());
            return List.of();
        }
    }
}
