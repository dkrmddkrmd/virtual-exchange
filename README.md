# 📈 실시간 대용량 가상 자산 거래 플랫폼 (Virtual Exchange)

> **대규모 트래픽 상황에서도 안정적인 주문 처리를 보장하는 가상 자산 모의 투자 서비스입니다.** > Upbit API를 활용한 실시간 시세 연동과 Kafka 기반의 비동기 주문 처리를 구현했습니다.

<br>

## 🛠 Tech Stack

| 분류 | 기술 스택                                               |
| :-- |:----------------------------------------------------|
| **Language** | Java 17                                             |
| **Framework** | Spring Boot 3.5.7, Spring Security, Spring Data JPA |
| **Database** | MySQL, Redis                                        |
| **Message Queue** | Apache Kafka                                        |
| **Infrastructure** | Docker, Docker Compose                              |
| **Testing** | JUnit5, JMeter                                      |
| **Tools** | IntelliJ, Git                                       |

<br>

## 🏛 Architecture
![아키텍처 다이어그램](assets/architecture.png)

* **Redis Caching:** 자주 조회되는 코인 시세 정보에 Look-aside 패턴 적용
* **Kafka Async Processing:** 주문 요청을 비동기 메시지로 발행하여 처리량 증대 및 결합도 감소
* **Scale-out Consideration:** Docker 컨테이너 기반의 확장성을 고려한 설계

<br>

## 🔥 Key Troubleshooting & Performance

### 1. 동시성 제어 및 대용량 트래픽 처리 (Redis → Kafka)
* **문제 상황:**
    * 초기에는 Redis 분산 락(Redisson)을 사용하여 동시성을 제어했으나, 동기 방식의 특성상 트래픽 급증 시 대기열 발생 및 응답 지연(Latency) 문제 확인.
    * JMeter 부하 테스트 결과, 평균 응답 시간 **6.7초** 소요.
* **해결 방안:**
    * **Kafka**를 도입하여 주문 처리를 **비동기(Event-Driven)** 방식으로 전환.
    * Kafka의 **Partition Key(UserId)** 전략을 사용하여, 사용자별 순서를 보장하면서도 별도의 락(Lock) 없이 데이터 정합성을 확보.
* **결과:**
    * 복잡한 락 로직을 제거하여 코드 복잡도 감소.
    * 평균 응답 시간 **0.5초**로 단축 (**약 92% 성능 개선**).
    * TPS(초당 처리량) **1,500+** 달성 및 안정성 검증 완료.

### 2. JPA N+1 문제 해결 및 쿼리 최적화
* **문제 상황:**
    * '내 자산 현황' 조회 시, 보유한 종목 개수(N)만큼 `Select` 쿼리가 추가로 발생하는 N+1 문제 발생.
* **해결 방안:**
    * `Fetch Join`을 적용하여 연관된 `Stock` 엔티티를 한 번의 쿼리로 함께 조회하도록 최적화.
* **결과:**
    * API 호출 당 쿼리 발생 횟수: **1 + N회 ➡ 1회**로 감소.

<br>

## ✨ Key Features
* **실시간 시세 조회:** Upbit API 연동 및 WebSocket/Scheduler를 통한 시세 동기화.
* **매수/매도 주문:** 지정가/시장가 주문 지원 (Kafka 기반 비동기 처리).
* **자산 관리:** 실시간 수익률 계산 및 포트폴리오 조회.
* **보안(Security):** JWT 기반 인증/인가 및 Spring Security Context 활용.

<br>

## 🧪 Testing (Stability)
* **JMeter Stress Test:** 1,000명 동시 접속, 총 10,000건 주문 요청 시 **Error 0%** 달성.
* **Unit/Integration Test:** JUnit5를 활용한 주요 비즈니스 로직(주문, 결제) 테스트 코드 작성.

<br>

## 🚀 How to Run
```bash
# 1. 프로젝트 클론
git clone [https://github.com/](https://github.com/)dkrmddkrmd/virtual-exchange.git

# 2. Docker 실행 (Kafka, Redis, MySQL)
docker-compose up -d

# 3. 애플리케이션 실행
./gradlew bootRun