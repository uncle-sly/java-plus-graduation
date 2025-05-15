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

import java.util.List;

@Slf4j
@Component
public class RestStatClient implements StatClient {
    private final DiscoveryClient discoveryClient;
    private RestClient restClient;

    @Autowired
    public RestStatClient(DiscoveryClient discoveryClient) {
        this.discoveryClient = discoveryClient;
    }


    @Override
    public void hit(ParamHitDto paramHitDto) {
        try {
            this.restClient = RestClient.create(getInstance().getUri().toString());

            restClient.post()
                    .uri("/hit")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(paramHitDto)
                    .retrieve()
                    .onStatus(status -> status != HttpStatus.CREATED, (request, response) -> {
                        throw new InvalidRequestException(response.getStatusCode().value() + ": " + response.getBody());
                    });
        } catch (Exception e) {
            log.info(e.getMessage());
        }

    }

    @Override
    public List<ViewStats> getStat(ParamDto paramDto) {
        try {
            this.restClient = RestClient.create(getInstance().getUri().toString());

            return restClient.get().uri(uriBuilder -> uriBuilder.path("/stats")
                            .queryParam("start", paramDto.getStart().toString())
                            .queryParam("end", paramDto.getEnd().toString())
                            .queryParam("uris", paramDto.getUris())
                            .queryParam("unique", paramDto.getUnique())
                            .build())
                    .retrieve()
                    .onStatus(status -> status != HttpStatus.OK, (request, response) -> {
                        throw new InvalidRequestException(response.getStatusCode().value() + ": " + response.getBody());
                    })
                    .body(new ParameterizedTypeReference<List<ViewStats>>() {
                    });
        } catch (Exception e) {
            log.info(e.getMessage());
            return List.of();
        }
    }


    private ServiceInstance getInstance() {
        try {
            return discoveryClient
                    .getInstances("stat-server")
                    .getFirst();
        } catch (Exception exception) {
            throw new StatsServerUnavailable(
                    "Ошибка обнаружения адреса сервиса статистики с id: " + "stat-server",
                    exception
            );
        }
    }
}