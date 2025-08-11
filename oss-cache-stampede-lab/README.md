# 캐시 스탬피드 및 회복성 실험실

이 모듈은 "캐시 스탬피드" (또는 "Thundering Herd") 문제를 방지하기 위한 다양한 캐싱 전략을 시연하고 비교하는 실습 환경을 제공합니다.

세 가지 독특한 전략을 실험해볼 수 있습니다:
1.  **SingleFlight (블로킹 방식)**: 데이터 일관성이 중요한 "핫 키(Hot Key)" 시나리오에 사용됩니다.
2.  **SWR (Stale-While-Revalidate)**: 사용자 경험(빠른 응답 시간)이 중요한 "핫 키" 시나리오에 사용됩니다.
3.  **Jitter**: "핫 키"가 아닌 일반적인 키들의 캐시 만료 시점을 분산시켜 부하를 조절할 때 사용됩니다.

## 실험실 실행 방법

1.  `oss-lab` 프로젝트의 루트 디렉토리로 이동합니다.
2.  Gradle Wrapper를 사용하여 애플리케이션을 실행합니다:
    ```bash
    ./gradlew :oss-cache-stampede-lab:bootRun
    ```
3.  애플리케이션이 `8080` 포트에서 시작됩니다.

## 실험 내용

웹 브라우저나 `curl`과 같은 커맨드 라인 도구를 사용하여 실험을 수행할 수 있습니다. 동시 요청을 시뮬레이션하려면 여러 개의 터미널 창에서 `curl`을 사용하는 것이 효과적입니다.

먼저, 모든 캐시를 지워 깨끗한 상태에서 시작합니다:
```bash
curl http://localhost:8080/lab/clear/all
```

---

### 실험 1: 순수 SingleFlight (블로킹 방식)

이 전략은 단 하나의 "리더(leader)" 요청만이 데이터를 가져오도록 하고 나머지 동시 요청들은 기다리게 만들어 리소스를 보호하는 방법을 보여줍니다.

**엔드포인트**: `GET /lab/singleflight/{key}`

**테스트 방법:**
1.  두세 개의 터미널 창을 엽니다.
2.  각 터미널에서 동일한 키(예: `hot-key`)에 대해 아래 명령어를 동시에 실행합니다:
    ```bash
    curl http://localhost:8080/lab/singleflight/hot-key
    ```

**예상 결과:**
*   **첫 번째 응답 (리더):** `Source: DB (Leader), Value: Data for hot-key`와 같은 응답을 받게 됩니다. 이 요청은 약 2초(DB 조회 시뮬레이션 시간)가 소요됩니다.
*   **다른 응답 (팔로워):** 다른 요청들은 거의 즉시 `Source: DB (Follower), Value: Data for hot-key`와 같은 응답을 받습니다. 이 요청들은 직접 DB 쿼리를 실행하지 않고 리더의 결과를 기다렸다가 받은 것입니다.
*   **이후 요청:** 명령어를 다시 실행하면, 캐시로부터 즉각적인 응답을 받게 됩니다: `Source: Cache, Value: Data for hot-key`.

---

### 실험 2: SWR (Stale-While-Revalidate)

이 전략은 오래된(stale) 데이터를 우선 반환하고 백그라운드에서 데이터를 갱신함으로써 어떻게 빠른 사용자 경험을 제공하는지 보여줍니다.

**엔드포인트**: `GET /lab/swr/{key}`
**TTL 설정**: Soft TTL = 5초, Hard TTL = 10초.

**테스트 방법:**
1.  **첫 요청:** 데이터를 처음으로 가져옵니다.
    ```bash
    curl http://localhost:8080/lab/swr/hot-key
    ```
    *   **결과:** `Source: DB (Blocking), Value: Data for hot-key @...`. 2초가 소요됩니다.

2.  **5초 이내 (신선한 캐시):** 5초 안에 다시 요청합니다.
    *   **결과:** `Source: Fresh Cache, Value: ...`. 즉시 응답합니다.

3.  **5초 이후 (Soft TTL 만료):** 5초를 기다린 후 요청을 보냅니다.
    ```bash
    curl http://localhost:8080/lab/swr/hot-key
    ```
    *   **결과:** `Source: Stale Cache, Value: ...`. 오래된 데이터를 **즉시** 받습니다.
    *   **백그라운드 동작:** 애플리케이션 로그를 확인해 보세요. `(SWR-Background) I am the LEADER. Refreshing data...`와 같은 로그 메시지를 볼 수 있습니다. 단 하나의 백그라운드 스레드가 캐시를 업데이트하고 있습니다. 이때 여러 요청을 보내도 오직 하나의 요청만이 갱신을 트리거합니다.

4.  **백그라운드 갱신 이후:** 다시 요청합니다.
    *   **결과:** 새로운 타임스탬프가 찍힌 데이터와 함께 `Source: Fresh Cache, Value: ...` 응답을 받습니다.

5.  **10초 이후 (Hard TTL 만료):** 마지막 갱신으로부터 10초를 기다린 후 요청합니다.
    *   **결과:** 블로킹 방식의 SingleFlight와 동일하게 동작합니다. 첫 요청은 새로운 데이터를 가져오는 동안 2초 동안 블로킹됩니다.

---

### 실험 3: Jitter

이 전략은 TTL에 무작위성을 추가하여 캐시 만료로 인한 부하를 시간적으로 분산시키는 데 어떻게 도움이 되는지 보여줍니다.

**엔드포인트**: `GET /lab/jitter/{key}`
**TTL 설정**: 기본 TTL = 10초, Jitter = ±5초.

**테스트 방법:**
1.  Jitter 캐시를 비웁니다:
    ```bash
    curl http://localhost:8080/lab/clear/jitter
    ```
2.  키를 처음으로 요청합니다:
    ```bash
    curl http://localhost:8080/lab/jitter/some-key
    ```
    *   **결과:** `Source: DB, Value: Data for some-key (Cached with TTL: 12s)`. 캐시된 TTL 값을 확인하세요.

3.  캐시를 비우고 다시 요청합니다:
    ```bash
    curl http://localhost:8080/lab/clear/jitter
    curl http://localhost:8080/lab/jitter/some-key
    ```
    *   **결과:** `Source: DB, Value: Data for some-key (Cached with TTL: 8s)`.

**예상 결과:**
캐시 미스를 발생시킬 때마다, 응답으로 오는 TTL 값은 5초(10-5)에서 15초(10+5) 사이의 무작위 값이 됩니다. 이는 여러 다른 키들의 만료 시점이 어떻게 분산되어, 동시에 만료되는 것을 방지하는지 보여줍니다.