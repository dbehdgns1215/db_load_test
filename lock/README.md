# 대규모 트래픽 대응 좌석 예약 시스템

멀티스레드 + 분산 환경에서 발생하는 **동시성 문제(Race Condition)** 를 재현하고, 다양한 락(Lock) 전략으로 단계적으로 해결해가는 학습 프로젝트입니다.

## 아키텍처

```
[JMeter 100명 동시 요청]
        ↓
   [Nginx 로드밸런서]
    ↓ (라운드로빈)
┌────────┬────────┬────────┐
│ App 1  │ App 2  │ App 3  │  ← Spring Boot 서버 3대
└────┬───┴────┬───┴────┬───┘
     └────────┼────────┘
              ↓
     [MySQL]  +  [Redis]
```

## 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Spring Boot 3.5, Java 21 |
| Database | MySQL 8.0 |
| Cache | Redis 7.0 |
| Load Balancer | Nginx (라운드로빈) |
| Container | Docker, Docker Compose |
| Load Testing | Apache JMeter 5.6 |

## 브랜치 구조

각 브랜치는 `main`(문제 상태)에서 분기하여, **같은 문제에 대한 서로 다른 해결 전략**을 적용합니다.

| 브랜치 | 설명 | 단일 서버 |          분산 환경          |
|--------|------|:-----:|:-----------------------:|
| `main` | **락 없음** — 동시성 문제 재현 | 중복 예약 |          중복 예약          |
| `phase/synchronized` | `synchronized` 키워드 적용 |  해결   | JVM 단위 락이라 서버 간 동기화 불가  |
| `phase/pessimistic-lock` | DB 비관적 락 (`SELECT FOR UPDATE`) |  해결   | 해결 (DB 커넥션 점유 + 데드락 위험) |
| `phase/optimistic-lock` | DB 낙관적 락 (`@Version`) |  해결   |    해결 (충돌 시 재시도 폭발)     |
| `phase/redis-distributed-lock` | Redis 분산 락 (Redisson) |  해결   |      해결 (DB 부담 없음)      |

## 핵심 문제: Race Condition

`main` 브랜치의 `SeatService`는 **의도적으로 락 없이** 구현되어 있습니다.

```java
Seat seat = seatRepository.findById(seatId);  // 1. 조회

if (seat.isReserved()) return "이미 예약됨";   // 2. 확인 - 동시에 통과!

Thread.sleep(100);                             // 3. 지연 (문제 확대)

seat.reserve(userName);                        // 4. 예약 - 중복 발생!
```

100명이 동시에 요청하면, 여러 스레드가 2번을 동시에 통과하여 **1개 좌석에 다수가 예약 성공**하는 문제가 발생합니다.

상세한 내용은 docs 디렉토리 내부에 문서화된 보고서를 참조해주세요.


## 실행 방법

### 1. Docker 환경 시작

```bash
docker-compose up -d --build
```

### 2. 초기 좌석 생성

```bash
curl -X POST "http://localhost/api/seats/init?count=10"
```

### 3. 동시성 테스트 (JMeter)

```bash
jmeter -n -t jmeter/100명이\ 동시에\ 한\ 좌석\ 예약.jmx -l results.jtl
```

### 4. 결과 확인

```bash
# 좌석 상태
curl http://localhost/api/seats/1

# 서버 로그에서 "예약 성공" 횟수 확인
docker-compose logs app1 app2 app3 | findstr "예약 성공"
```

> **정상이라면 1건만 성공해야 하지만, `main` 브랜치에서는 다수가 성공합니다.**

### 다음 단계

- Phase 1: 인프라 구축 및 동시성 문제 재현 
- Phase 2-1: synchronized → 단일 서버에서는 OK, 분산 환경에서는 FAIL 
- Phase 2-2: 비관적 락 -> 분산 OK, 근데 DB 커넥션 점유 + 데드락 위험 
- Phase 2-3: 낙관적 락 -> 분산 OK, 근데 100명 동시면 99명 재시도 -> 성능 폭발 
- Phase 3: Redis 분산 락 -> 분산 OK + DB 부담 없음 + 성능 좋음


## API

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/seats/init?count=10` | 초기 좌석 데이터 생성 |
| GET | `/api/seats/{id}` | 좌석 조회 |
| POST | `/api/seats/reserve` | 좌석 예약 |
| POST | `/api/seats/{id}/cancel` | 예약 취소 |
| GET | `/api/seats/health` | 헬스체크 |

## 환경 종료

```bash
docker-compose down -v
```


