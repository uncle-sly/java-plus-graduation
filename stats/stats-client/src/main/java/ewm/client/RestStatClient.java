package ewm.client;

import ewm.ParamDto;
import ewm.ParamHitDto;
import ewm.ViewStats;
import ewm.exception.InvalidRequestException;
import ewm.exception.StatsServerUnavailable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class RestStatClient implements StatClient {
    private static final String STAT_SERVER_ID = "stat-server";
    private static final String HIT_ENDPOINT = "/hit";
    private static final String STATS_ENDPOINT = "/stats";

    private final DiscoveryClient discoveryClient;
    private RestClient restClient;
    private URI currentUri;

    @Autowired
    public RestStatClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }

    @Override
    public void hit(ParamHitDto paramHitDto) {
        try {
            getRestClient().post()
                    .uri(HIT_ENDPOINT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(paramHitDto)
                    .retrieve()
                    .onStatus(status -> status != HttpStatus.CREATED, (request, response) -> {
                        throw new InvalidRequestException(response.getStatusCode().value() + ": " + response.getBody());
                    })
                    .body(ViewStats.class);


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
                        throw new InvalidRequestException(response.getStatusCode().value() + ": " + response.getBody());
                    })
//                    .body(new ParameterizedTypeReference<>() {
                    .body(new ParameterizedTypeReference<List<ViewStats>>() {
                    });
        } catch (Exception e) {
            log.warn("Ошибка при получении статистики: {}", e.getMessage());
            return List.of();
        }
    }

    private ServiceInstance getInstance() {
        try {
            return discoveryClient.getInstances(STAT_SERVER_ID)
                    .getFirst();
        } catch (Exception exception) {
            throw new StatsServerUnavailable("Ошибка обнаружения сервиса статистики: " + STAT_SERVER_ID, exception);
        }
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