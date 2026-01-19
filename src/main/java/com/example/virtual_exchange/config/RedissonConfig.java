package com.example.virtual_exchange.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration // 1. "스프링아, 이건 설정 파일이야."
public class RedissonConfig {
    @Bean // 2. "이 메서드가 만드는 객체(RedissonClient)를 네가 관리해줘."
    public RedissonClient redissonClient() {
        Config config = new Config();

        // 3. "우리는 서버 1개짜리(Single) 쓸 거고, 주소는 내 컴퓨터(localhost)의 6379번 포트야."
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");

        // 4. "설정 끝! 이제 연결해서 클라이언트 객체 만들어!"
        return Redisson.create(config);
    }
}