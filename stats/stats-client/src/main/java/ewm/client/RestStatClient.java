package ewm.client;

import ewm.ParamDto;
import ewm.ParamHitDto;
import ewm.ViewStats;
import ewm.exception.InvalidRequestException;
import ewm.exception.StatsServerUnavailable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RestStatClient implements StatClient {
    private static final String STAT_SERVER_ID = "stats-server";
    private static final String HIT_ENDPOINT = "/hit";
    private static final String STATS_ENDPOINT = "/stats";

    private final DiscoveryClient discoveryClient;
    private RestClient restClient;
    private URI currentUri;


    @Override
    public void hit(ParamHitDto paramHitDto) {
        try {
            getRestClient().post()
                    .uri(HIT_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(paramHitDto)
                    .retrieve()
                    .onStatus(status -> status != HttpStatus.CREATED, (request, response) -> {
                        throw new InvalidRequestException(InvalidRequestException.class, response.getStatusCode().value() + ": " + response.getBody());
                    });

        } catch (Exception e) {
            log.warn("Ошибка при отправке hit-запроса: {}", e.getMessage());
        }
    }

    @Override
    public List<ViewStats> getStat(ParamDto paramDto) {
        try {
            return getRestClient().get().uri(uriBuilder -> uriBuilder.path(STATS_ENDPOINT)
                            .queryParam("start", paramDto.getStart().toString())
                            .queryParam("end", paramDto.getEnd().toString())
                            .queryParam("uris", paramDto.getUris())
                            .queryParam("unique", paramDto.getUnique())
                            .build())
                    .retrieve()
                    .onStatus(status -> status != HttpStatus.OK, (request, response) -> {
                        throw new InvalidRequestException(InvalidRequestException.class, response.getStatusCode().value() + ": " + response.getBody());
                    })
                    .body(new ParameterizedTypeReference<>() {
                    });
        } catch (Exception e) {
            log.warn("Ошибка при получении статистики: {}", e.getMessage());
            return List.of();
        }
    }

    private synchronized ServiceInstance getInstance() {
        List<ServiceInstance> instances = discoveryClient.getInstances(STAT_SERVER_ID);

        if (instances == null || instances.isEmpty()) {
            log.warn("Не найдено ни одного экземпляра сервиса: {}", STAT_SERVER_ID);
            throw new StatsServerUnavailable(StatsServerUnavailable.class, "Ошибка: сервис статистики не найден: " + STAT_SERVER_ID);
        }
        return instances.getFirst();
    }

    private RestClient getRestClient() {
        ServiceInstance instance = getInstance();
        URI newUri = instance.getUri();

        if (restClient == null || !newUri.equals(currentUri)) {
            log.info("Обновление URI RestClient с {} на {}", currentUri, newUri);
            this.restClient = RestClient.create(newUri.toString());
            this.currentUri = newUri;
        }
        return this.restClient;
    }

}