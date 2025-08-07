# Elasticsearch 샤드 부하 시나리오 실험

이 모듈은 Elasticsearch 클러스터에서 특정 샤드에 부하가 집중될 때 발생할 수 있는 문제 상황을 시뮬레이션하고 테스트하기 위한 것입니다.

## 주요 실험 시나리오

### 시나리오 1: 샤드 리밸런싱으로 인한 노드 과부하

**가설**: 접근 빈도가 매우 높은 거대 샤드(Hot & Large Shard)가 리밸런싱으로 인해 특정 데이터 노드에 중복 할당될 경우, 해당 노드는 과부하 상태가 되어 클러스터 전체의 안정성을 저해할 수 있다.

**실험 절차**:
1. 3개 이상의 데이터 노드로 구성된 Elasticsearch 클러스터를 실행합니다.
2. 특정 인덱스(`hot-index-1`)를 생성하고, 대량의 데이터를 색인하여 "Hot & Large Shard"를 만듭니다. (예: Primary 1, Replica 1)
3. `hot-index-1`의 Primary 샤드는 `node-1`, Replica 샤드는 `node-2`에 할당된 것을 확인합니다.
4. `hot-index-1`에 지속적인 읽기/쓰기 부하를 가합니다.
5. 클러스터 설정을 변경하거나 특정 노드를 제외(exclude)시켜 `node-2`에 있던 Replica 샤드가 `node-3`으로 강제 리밸런싱되도록 유도합니다.
6. 또 다른 고부하 인덱스(`hot-index-2`)의 샤드 또한 `node-3`으로 할당되도록 시나리오를 설계합니다.
7. `node-3`이 두 개의 "Hot & Large Shard"를 동시에 처리하게 될 때, 해당 노드의 CPU, JVM Heap Memory, 응답 시간 등 주요 지표를 모니터링하고 문제를 관찰합니다.

### 시나리오 2: 노드 장애 및 Replica 승격으로 인한 과부하

**가설**: 접근 빈도가 높은 샤드를 가진 데이터 노드가 갑자기 다운될 경우, 해당 샤드의 Replica가 Primary로 즉시 승격되면서 모든 트래픽을 받게 되어 해당 노드가 과부하에 빠질 수 있다.

**실험 절차**:
1. 3개 이상의 데이터 노드로 구성된 Elasticsearch 클러스터를 실행합니다.
2. `hot-index-1`을 생성하고 Primary 샤드는 `node-1`, Replica 샤드는 `node-2`에 할당합니다.
3. `hot-index-1`에 지속적인 읽기/쓰기 부하를 가합니다.
4. Primary 샤드가 위치한 `node-1`을 갑자기 중지시킵니다. (예: `docker stop <node-1-container>`)
5. Elasticsearch 클러스터는 `node-2`에 있던 Replica 샤드를 새로운 Primary 샤드로 승격시킵니다.
6. 기존 `node-1`로 향하던 모든 트래픽이 이제 `node-2`로 집중됩니다.
7. `node-2`의 시스템 부하(CPU, Memory), 응답 시간 등을 측정하여 Replica 승격이 서비스에 미치는 영향을 분석합니다.

## 실행 방법
(추후 애플리케이션 구현 후 작성 예정)
