# Kafka 심층 운영 실험 계획

## 1. 실험 목표

본 실험은 Kafka를 단순 메시지 큐로 사용하는 것을 넘어, 대용량 데이터 파이프라인의 핵심 구성요소로서 Kafka를 심층적으로 이해하고 운영 역량을 강화하는 것을 목표로 한다. 다음의 실제 운영 시나리오를 통해 시스템의 한계를 파악하고, 문제 해결 능력을 기른다.

-   **데이터 유실 시나리오 재현**: '느린 컨슈머(Slow Consumer)' 문제로 인해 데이터가 유실되는 상황을 직접 재현하고, 그 근본 원인을 분석한다.
-   **성능과 안정성의 트레이드오프 분석**: `acks`, `retention`, `batching` 등 주요 파라미터가 시스템 전체 처리량(Throughput)과 데이터 보장 수준(Durability)에 미치는 영향을 정량적으로 측정하고 비교한다.
-   **모니터링 기반 문제 해결**: Prometheus와 Grafana를 이용해 Consumer Lag, 디스크 사용량 등 핵심 지표를 실시간으로 추적하고, 시스템의 병목 지점을 시각적으로 식별하는 경험을 쌓는다.

## 2. 핵심 시나리오: "느린 컨슈머"와 데이터 유실

하나의 토픽을 처리 속도가 다른 두 종류의 컨슈머가 구독하는 현실적인 상황을 가정한다.

-   **프로듀서**: 초당 수천 건의 대용량 이벤트를 지속적으로 발행한다.
-   **빠른 컨슈머**: 실시간 알림 등 즉각적인 처리가 필요한 애플리케이션.
-   **느린 컨슈머**: 외부 API 호출, DB 저장 등 복잡한 로직으로 인해 메시지 처리 속도가 느린 애플리케이션.

이 시나리오에서 **느린 컨슈머의 처리 속도가 프로듀서의 발행 속도를 따라가지 못하면 Consumer Lag이 누적**되고, 결국 Kafka 브로커의 **로그 보존 정책(`log.retention`)에 의해 아직 처리하지 못한 데이터가 삭제**되는 상황을 관찰하는 것이 핵심이다.

## 3. 시스템 아키텍처

본 실험 환경은 Docker Compose를 통해 구축되며, 실제 운영 환경과 유사한 데이터 파이프라인 및 모니터링 시스템으로 구성됩니다.

```mermaid
flowchart LR
  subgraph Application Layer
    A[Producer Service<br/>(Spring Kafka)]
    B1[Fast Consumer Group]
    B2[Slow Consumer Group]
  end

  subgraph Kafka Cluster
    K[Kafka Broker]
    KJM[X] -->|JMX Metrics| JM[JMX Exporter]
  end

  subgraph Monitoring Layer
    JM --> P[Prometheus]
    P --> G[Grafana]
  end

  %% Data Flow
  A -->|produce events| K
  K -->|consume| B1
  K -->|consume| B2

  %% Metrics Flow
  K -->|JMX| JM
  P -->|scrape| JM
  G -->|visualize| P

  style KJM fill:#f9f,stroke:#333,stroke-width:1px
  linkStyle default stroke:#777,stroke-width:1px,stroke-dasharray:5 5
```

### 구성 요소 및 역할

-   **Java Application (Producer/Consumers)**
    -   **Producer**: `ProducerController`를 통해 REST API(`GET /start/{tps}`) 요청을 받으면, 지정된 속도(tps)로 Kafka 토픽에 메시지를 발행합니다.
    -   **Consumers**: 동일한 토픽을 구독하지만, 서로 다른 `groupId`를 가진 두 종류의 컨슈머가 동작합니다.
        -   `fast-consumer-group`: 메시지를 즉시 처리하는 빠른 컨슈머.
        -   `slow-consumer-group`: `Thread.sleep()`으로 의도적인 지연을 발생시키는 느린 컨슈머.

-   **Kafka**
    -   메시지를 수신하고 디스크에 저장하는 메시지 브로커입니다.
    -   JMX (Java Management Extensions)를 통해 내부 동작 상태(메트릭)를 외부로 노출합니다.

-   **JMX Exporter**
    -   Kafka의 JMX 메트릭을 Prometheus가 수집할 수 있는 HTTP 엔드포인트 형태로 변환해주는 어댑터입니다.

-   **Prometheus**
    -   JMX Exporter가 노출한 엔드포인트를 주기적으로 방문(Scrape)하여 Kafka의 메트릭을 수집하고 시계열 데이터베이스(TSDB)에 저장합니다.

-   **Grafana**
    -   Prometheus에 저장된 메트릭 데이터를 쿼리하여 시각적인 대시보드를 만듭니다. 사용자는 이 대시보드를 통해 Consumer Lag, 처리량, 디스크 사용량 등을 실시간으로 모니터링할 수 있습니다.

### 데이터 흐름과 메트릭 흐름

이 아키텍처에는 두 가지 주요 흐름이 존재합니다.

1.  **데이터 흐름 (실선)**: 비즈니스 데이터가 흘러가는 경로입니다.
    `Java Producer` → `Kafka Topic` → `Java Consumers (Fast & Slow)`

2.  **메트릭 흐름 (점선)**: 시스템의 상태를 모니터링하기 위한 데이터 경로입니다.
    `Kafka (JMX)` → `JMX Exporter` → `Prometheus` → `Grafana` → `사용자 웹 브라우저`

## 4. 환경 구성 파일

-   `docker-compose.yml`: 위 아키텍처의 모든 컴포넌트(Kafka, JMX Exporter, Prometheus, Grafana)를 정의하고 네트워크로 연결합니다.
-   `prometheus.yml`: Prometheus가 `jmx-exporter`를 주기적으로 스크랩하도록 설정합니다.
-   `src/main/java/...`: Producer, Fast Consumer, Slow Consumer 역할을 수행할 Spring for Apache Kafka 애플리케이션 코드입니다.


## 5. 실험 시나리오 계획

**A. 데이터 유실 재현**
1.  Kafka 토픽의 `retention.bytes`를 10MB로 낮게 설정한다.
2.  프로듀서를 실행하여 10MB 이상의 데이터를 빠르게 발행한다.
3.  느린 컨슈머와 빠른 컨슈머를 동시에 실행한다.
4.  Grafana 대시보드에서 느린 컨슈머의 Lag이 급증하고, Kafka 브로커의 디스크 사용량이 10MB에 도달했을 때 Lag이 갑자기 감소(리셋)하는 현상을 관찰한다. → **데이터 유실 발생 지점**

**B. 데이터 유실 방지**
1.  `retention.bytes`를 1GB로 충분히 늘려 데이터 유실이 발생하지 않음을 확인한다.
2.  이를 통해 **디스크 비용과 데이터 안정성 간의 트레이드오프**를 고찰한다.

**C. 프로듀서 `acks` 설정 변경**
1.  `acks=1`과 `acks=all` 설정에 따른 프로듀서의 발행 성능(Throughput) 차이를 측정한다.
2.  `acks=all`과 `min.insync.replicas=2` 조합으로 브로커 장애 상황에서도 데이터 유실을 방지할 수 있는지 확인한다. (이를 위해서는 Kafka 클러스터 구성 필요)

## 6. 환경 정리

실험 완료 후, 아래 명령어로 모든 리소스를 정리한다.

```bash
docker-compose down -v
```