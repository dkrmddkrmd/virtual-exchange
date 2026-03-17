package com.example.virtual_exchange.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    // "stock_order"라는 토픽이 없으면 파티션 3개짜리로 새로 만듭니다.
    // (이미 있다면 이 설정은 무시될 수 있으니, 안 되면 Docker 카프카를 초기화하거나 토픽명을 v2로 바꾸세요)
    @Bean
    public NewTopic stockOrderTopic() {
        return TopicBuilder.name("stock_order") // 프로듀서/컨슈머에서 쓰는 토픽 이름과 같아야 함
                .partitions(3)                  // 파티션 개수 3개
                .replicas(1)                    // 로컬이라 복제본은 1개
                .build();
    }
}