package tomcat.controller;

import java.util.concurrent.ForkJoinPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;

@Slf4j
@RestController
public class TestController {

  @GetMapping("/hello2")
  public DeferredResult<String> hello2() {
    log.info("1) 요청 스레드 진입 - DeferredResult 반환 전");

    DeferredResult<String> deferredResult = new DeferredResult<>(30_000L, "타임아웃 발생!");

    log.info("2) 컨트롤러에서 DeferredResult 객체 생성 및 반환 준비");

    ForkJoinPool.commonPool().submit(() -> {
      log.info("3) 별도 스레드에서 비즈니스 로직 시작");
      try {
        // 오래 걸리는 작업 예시 (5초 지연)
        Thread.sleep(5_000L);
        log.info("4) 별도 스레드에서 비즈니스 로직 완료");
        deferredResult.setResult("작업 완료!");
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        deferredResult.setErrorResult(e);
      }
    });

    log.info("5) 컨트롤러에서 즉시 반환 - 요청 스레드 해제");

    deferredResult.onCompletion(() -> log.info("6) 응답 완료"));
    return deferredResult;
  }
}


