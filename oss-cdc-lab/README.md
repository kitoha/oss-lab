# OSS CDC Lab: Outbox Pattern Resilience Test

## 개요
이 프로젝트는 CDC(Change Data Capture) 기반의 아웃박스 패턴(Outbox Pattern)에서 CDC 프로세스에 장애가 발생했을 때, 시스템이 어떻게 데이터 정합성을 복구하는지 실험하고 검증하기 위한 것입니다.

Debezium을 CDC 도구로 사용하여 PostgreSQL 데이터베이스의 변경 사항을 캡처하고, Kafka를 통해 이벤트를 전파합니다.

## 실험 시나리오
1.  **정상 상태**: Spring Boot 애플리케이션이 Outbox 테이블에 데이터를 삽입하면, Debezium이 이를 감지하여 Kafka 토픽으로 메시지를 발행하고, Consumer가 이 메시지를 수신합니다.
2.  **장애 상황**: Debezium 커넥터를 일시적으로 중단시켜 CDC 프로세스에 장애를 시뮬레이션합니다.
3.  **데이터 추가**: 커넥터가 중단된 상태에서 애플리케이션은 계속해서 Outbox 테이블에 새로운 데이터를 추가합니다. 이 데이터는 Kafka로 즉시 발행되지 않습니다.
4.  **복구**: 중단되었던 Debezium 커넥터를 재시작합니다.
5.  **검증**: 커넥터가 재시작된 후, 중단 시간 동안 쌓였던 모든 데이터 변경분이 누락 없이 Kafka로 발행되어 최종적으로 데이터 정합성이 복구되는 것을 확인합니다.

## 기술 스택
- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Kafka & Zookeeper
- Debezium (Kafka Connect)
- Docker & Docker Compose

## 실행 방법
1. Docker Compose를 사용하여 인프라를 실행합니다.
   ```bash
   docker-compose up -d
   ```

2. Spring Boot 애플리케이션을 실행합니다.

3. (실험 진행)
