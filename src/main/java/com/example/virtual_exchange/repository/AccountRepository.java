package com.example.virtual_exchange.repository;

import com.example.virtual_exchange.domain.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUserId(Long userId);

    // ★ [핵심] 비관적 락(Pessimistic Lock)을 건 조회 메서드 추가
    // "이 데이터 내가 쓸 거니까(WRITE), 트랜잭션 끝날 때까지 아무도 손대지 마!"
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.user.id = :userId")
    Optional<Account> findByUserIdWithLock(@Param("userId") Long userId);
}
