package hello.login.web.item;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;

@Component
public class NasaApiClient {
    private final WebClient webClient;

    public NasaApiClient(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://api.nasa.gov/planetary/").build();
    }

    public Mono<NasaApiResponse> getNasaImageOfTheDay() {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("apod")
                        .queryParam("api_key", "fcBF7Ad3AA0uYdzoo60mdPuCGqzkakuAJIOb7Oox")
                        .build())
                .retrieve()
                .bodyToMono(NasaApiResponse.class)
                .flatMap(response -> {
                    if ("image".equals(response.getMedia_type())) {
                        response.setUrl("data:image/jpeg;base64,");
                        return webClient.get()
                                .uri(response.getHdurl())
                                .retrieve()
                                .bodyToMono(byte[].class)
                                .map(imageData -> {
                                    String base64Image = java.util.Base64.getEncoder().encodeToString(imageData);
                                    response.setUrl(response.getUrl() + base64Image);
                                    return response;
                                });
                    }
                    return Mono.just(response);
                });
    }
}
