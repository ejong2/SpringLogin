package hello.login.web.item;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@RestController
public class NasaApiController {

    private WebClient webClient;
    private NasaApiClient nasaApiClient;

    public NasaApiController(NasaApiClient nasaApiClient) {
        this.nasaApiClient = nasaApiClient;
    }

    public NasaApiController() {
        this.webClient = WebClient.create("https://api.nasa.gov/planetary");
    }

    @GetMapping("/nasa/image-of-the-day")
    public Mono<NasaApiResponse> getNasaImageOfTheDay() {
        return webClient.get().uri(uriBuilder -> uriBuilder.path("/apod")
                        .queryParam("api_key", "fcBF7Ad3AA0uYdzoo60mdPuCGqzkakuAJIOb7Oox")
                        .build())
                .retrieve()
                .bodyToMono(NasaApiResponse.class);
    }
    @GetMapping("/nasa/image-of-the-day2")
    public Mono<String> getNasaImageOfTheDay2(Model model) {
        LocalDate date = LocalDate.of(2020, Month.JANUARY, 1);
        String formattedDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
        return webClient.get()
                .uri("https://api.nasa.gov/planetary/apod?date=2020-01-01&api_key=fcBF7Ad3AA0uYdzoo60mdPuCGqzkakuAJIOb7Oox")
                .retrieve()
                .bodyToMono(NasaApiResponse.class)
                .flatMap(response -> {
                    if ("image".equals(response.getMediaType())) {
                        return webClient.get()
                                .uri(response.getUrl())
                                .accept(MediaType.IMAGE_JPEG)
                                .retrieve()
                                .bodyToMono(byte[].class)
                                .map(imageData -> {
                                    try (OutputStream out = new FileOutputStream("image.jpg")) {
                                        out.write(imageData);
                                        return "Image saved successfully!";
                                    } catch (IOException e) {
                                        throw new RuntimeException("Error saving image", e);
                                    }
                                });
                    } else {
                        return Mono.just("Not an image response");
                    }
                });
    }
    @GetMapping("/nasa/image-of-the-day3")
    public Mono<String> getNasaImageOfTheDay3(Model model) {
        String url = "https://api.nasa.gov/planetary/apod";
        String apiKey = "fcBF7Ad3AA0uYdzoo60mdPuCGqzkakuAJIOb7Oox";
        LocalDate date = LocalDate.of(2020, 1, 1); // 2020년 1월 1일
        String dateString = date.format(DateTimeFormatter.ISO_DATE);

        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path(url)
                        .queryParam("api_key", apiKey)
                        .queryParam("date", dateString)
                        .build())
                .retrieve()
                .bodyToMono(NasaApiResponse.class)
                .flatMap(response -> {
                    if ("image".equals(response.getMediaType())) {
                        String imageUrl = response.getUrl();
                        String imageFileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
                        String imagePath = "C:/images/" + imageFileName; // 이미지 파일 저장 경로

                        return webClient.get()
                                .uri(imageUrl)
                                .accept(MediaType.IMAGE_JPEG)
                                .retrieve()
                                .bodyToMono(byte[].class)
                                .map(imageData -> {
                                    try {
                                        // 이미지 파일 저장
                                        Files.write(Paths.get(imagePath), imageData);

                                        // Model 객체에 이미지 파일 경로 추가
                                        model.addAttribute("imagePath", "/images/" + imageFileName);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    return "nasa-image-of-the-day";
                                });
                    } else {
                        return Mono.just("nasa-image-of-the-day");
                    }
                });
    }
    @GetMapping("/image")
    public String getNasaImageOfTheDay(Model model) throws IOException {
        NasaApiResponse response = webClient.get()
                .uri("https://api.nasa.gov/planetary/apod?api_key=fcBF7Ad3AA0uYdzoo60mdPuCGqzkakuAJIOb7Oox")
                .retrieve()
                .bodyToMono(NasaApiResponse.class)
                .block();

        if ("image".equals(response.getMediaType())) {
            String image = getImageData(response);
            model.addAttribute("image", image);
        } else {
            throw new RuntimeException("Response is not an image");
        }

        return "nasa-image";
    }

    private String getImageData(NasaApiResponse response) throws IOException {
        if ("image".equals(response.getMediaType())) {
            byte[] imageData = webClient.get()
                    .uri(response.getUrl())
                    .accept(MediaType.IMAGE_JPEG)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
            return Base64.getEncoder().encodeToString(imageData);
        }
        throw new RuntimeException("Response is not an image");
    }
}