# DB Study

데이터베이스 관련 개념을 직접 구현하고 실험하며 학습하는 레포지토리입니다.

단순히 이론을 정리하는 것이 아니라, **최대한 실제 문제 상황을 재현하고 해결하는 과정**을 코드와 문서로 기록하려 합니다.

## 프로젝트 목록

| 폴더 | 주제 | 설명 |
|------|------|------|
| [lock/](./lock) | 동시성 제어 | 좌석 예약 시스템에서 Race Condition을 재현하고, 다양한 락 전략(synchronized, DB Lock, Redis 분산 락)으로 단계적으로 해결합니다. |

## 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Spring Boot 3.5, Java 21 |
| Database | MySQL 8.0 |
| Cache | Redis 7.0 |
| Infra | Docker, Docker Compose, Nginx |
| Testing | Apache JMeter 5.6 |

> 각 프로젝트의 상세 내용은 해당 폴더의 README를 참조해주세요.
