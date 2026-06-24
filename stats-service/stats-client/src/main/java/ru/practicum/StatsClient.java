package ru.practicum;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StatsClient {

    private final RestClient restClient;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void saveHit(EndpointHitDto dto) {
        restClient.post()
                .uri("/hit")
                .body(dto)
                .retrieve()
                .toBodilessEntity();
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.fromPath("/stats")
                .queryParam("start", format(start))
                .queryParam("end", format(end))
                .queryParam("unique", unique);

        Optional.ofNullable(uris)
                .filter(list -> !list.isEmpty())
                .ifPresent(list -> uriComponentsBuilder.queryParam("uris", list.toArray()));

        String url = uriComponentsBuilder.build().toUriString();

        ParameterizedTypeReference<List<ViewStatsDto>> typeReference =
                new ParameterizedTypeReference<>() {
                };

        return restClient.get()
                .uri(url)
                .retrieve()
                .body(typeReference);
    }

    private String format(LocalDateTime localDateTime) {
        return localDateTime.format(FORMATTER);
    }
}