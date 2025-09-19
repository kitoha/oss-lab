package practice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
public class ExternalApiController {

    @GetMapping("/external/slow-api")
    public ResponseEntity<String> slowApi() throws InterruptedException {

        TimeUnit.MILLISECONDS.sleep(200);
        return ResponseEntity.ok("Response from slow external API");
    }

    @GetMapping("/external/very-slow-api")
    public ResponseEntity<String> verySlowApi() throws InterruptedException {
        TimeUnit.SECONDS.sleep(2);
        return ResponseEntity.ok("Response from very slow external API");
    }
}
