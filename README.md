# 선착순 쿠폰 발급 고트래픽 연습 프로젝트

대량 트래픽 환경에서 쿠폰 발급을 단계적으로 개선해보는 학습용 프로젝트입니다.  
동일한 도메인(쿠폰 선착순 발급)을 기준으로 아래 3가지 방식을 비교합니다.

- `coupon-issue-service`: DB + 애플리케이션 락 기반
- `coupon-issue-service-redis`: Redis Lua 기반 재고 차감 + 비동기 반영
- `coupon-issue-service-kafka`: Redis 선처리 + Kafka 비동기 적재(CQRS 성격)

## 프로젝트 구성

- `coupon-master-service`: 쿠폰 생성/조회, 공통 마스터 관리
- `coupon-issue-service`: 1단계 발급 서버(포트 `8080`)
- `coupon-issue-service-redis`: 2단계 발급 서버(포트 `9090`)
- `coupon-issue-service-kafka`: 3단계 발급 서버(포트 `9090`)

## 공통 실행 환경

- Java 17
- MariaDB (`localhost:3306`, DB: `coupon`)
- Redis (`localhost:6379`)
- Kafka (`localhost:9092`, 3단계에서 사용)
- k6 

## 단계별 설계 요약

### 1단계: 단일 DB 기반 발급

목표
- 트랜잭션 단위의 정확한 선착순 처리

핵심 포인트
- `@Transactional`로 쿠폰 차감/발급내역 저장 일관성 유지
- `ReentrantLock`으로 단일 인스턴스 내 동시성 제어

장점
- 구현이 가장 단순하고 디버깅이 쉬움
- 트랜잭션 경계가 명확함

단점
- 락 경합이 커지면 응답 지연 급증
- 인스턴스 확장 시 분산 락 부재

### 2단계: Redis 기반 발급 + 비동기 동기화

목표
- 발급 경로에서 DB 병목 감소, Redis 원자 연산으로 처리량 향상

핵심 포인트
- Redis Lua 스크립트로 재고 차감 + 중복 발급 체크를 원자적으로 처리
- 발급 사용자 ID를 Redis 자료구조에 적재 후 별도 동기화

장점
- DB 직접 쓰기 부담 완화
- 선착순/중복 체크를 Redis에서 빠르게 수행

단점
- Redis/DB 이중 저장소 정합성 관리 필요
- 동기화 실패/지연 시 운영 복잡도 증가

### 3단계: Redis + Kafka 기반

목표
- 발급 API를 최대한 경량화하고 DB 반영을 비동기 이벤트로 분리

핵심 포인트
- Redis Lua로 재고/중복 체크 후 Kafka로 발급 이벤트 발행
- 소비자에서 최종 DB 반영(CQRS 성격)

장점
- 가장 높은 처리량과 낮은 API 지연
- 트래픽 급증 시 완충(버퍼링) 효과

단점
- Kafka 운영/모니터링 부담
- 최종 일관성 모델 이해 필요

## 부하 테스트 방법

아래 스크립트를 그대로 사용했습니다.

- `coupon-issue-service/load-test.js`
- `coupon-issue-service-redis/load-test.js`
- `coupon-issue-service-kafka/load-test.js`

공통 시나리오
- `vus: 5000`
- `duration: 10s`
- 요청: `POST /coupon/issue/{couponId}/{userId}`

실행 예시

```bash
# 1단계
cd coupon-issue-service
./gradlew bootRun

# 별도 터미널
cd ..
k6 run coupon-issue-service/load-test.js
```

## 실측 결과 (2026-02-09, 로컬 1회 측정)

| 구분 | avg | med | p90 | p95 | max | 실패율 | req/s |
|---|---:|---:|---:|---:|---:|---:|---:|
| 1단계 DB | 18.73s | 18.23s | 35.05s | 35.36s | 35.85s | 9.66% | 138.75 |
| 2단계 Redis | 1.20s | 1.23s | 1.35s | 1.41s | 1.91s | 0.00% (3/39405) | 3436.28 |
| 3단계 Redis+Kafka | 283.71ms | 232.76ms | 532.24ms | 638ms | 1.37s | 0.00% | 11782.72 |

해석
- 1단계는 단일 락 경합으로 지연이 크게 증가해 실패율이 의미 있게 발생
- 2단계는 Redis 원자 처리로 급격히 개선
- 3단계는 Kafka 비동기화까지 적용되며 가장 높은 처리량 달성

## 참고 사항

- 본 수치는 로컬 단일 머신 기준이며, 머신 상태/기동 순서/기존 데이터 양에 따라 달라질 수 있습니다.
- 스크립트가 `userId`를 URL path로 직접 넣기 때문에 k6에서 high-cardinality 경고가 발생할 수 있습니다.

