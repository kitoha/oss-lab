package practice;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ComparisonController {

    private final RestTemplate restTemplate;


    public ComparisonController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/mvc")
    public String mvc() {
        return restTemplate.getForObject("/external/slow-api", String.class);
    }

    // WebFlux - 논블로킹 반응형
//    @GetMapping("/webflux")
//    public Mono<String> webflux() {
//        return webClient.get()
//            .uri("/external/slow-api")
//            .retrieve()
//            .bodyToMono(String.class);
//    }

    @GetMapping("/virtual")
    public String virtual() {
        return restTemplate.getForObject("/external/slow-api", String.class);
    }

    @GetMapping("/mvc/multiple")
    public Map<String, String> mvcMultiple() {
        Map<String, String> results = new HashMap<>();

        // 순차적으로 3번의 외부 API 호출 (블로킹)
        results.put("call1", restTemplate.getForObject("/external/slow-api", String.class));
        results.put("call2", restTemplate.getForObject("/external/slow-api", String.class));
        results.put("call3", restTemplate.getForObject("/external/slow-api", String.class));

        return results;
    }

//    @GetMapping("/webflux/multiple")
//    public Mono<Map<String, String>> webfluxMultiple() {
//        // 병렬로 3번의 외부 API 호출 (논블로킹)
//        Mono<String> call1 = webClient.get().uri("/external/slow-api").retrieve().bodyToMono(String.class);
//        Mono<String> call2 = webClient.get().uri("/external/slow-api").retrieve().bodyToMono(String.class);
//        Mono<String> call3 = webClient.get().uri("/external/slow-api").retrieve().bodyToMono(String.class);
//
//        return Mono.zip(call1, call2, call3)
//            .map(tuple -> {
//                Map<String, String> results = new HashMap<>();
//                results.put("call1", tuple.getT1());
//                results.put("call2", tuple.getT2());
//                results.put("call3", tuple.getT3());
//                return results;
//            });
//    }

    @GetMapping("/virtual/multiple")
    public Map<String, String> virtualMultiple() throws InterruptedException, ExecutionException {
        Map<String, String> results = new HashMap<>();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            Future<String> future1 = executor.submit(() ->
                restTemplate.getForObject("/external/slow-api", String.class));
            Future<String> future2 = executor.submit(() ->
                restTemplate.getForObject("/external/slow-api", String.class));
            Future<String> future3 = executor.submit(() ->
                restTemplate.getForObject("/external/slow-api", String.class));

            results.put("call1", future1.get());
            results.put("call2", future2.get());
            results.put("call3", future3.get());
        }

        return results;
    }
}
