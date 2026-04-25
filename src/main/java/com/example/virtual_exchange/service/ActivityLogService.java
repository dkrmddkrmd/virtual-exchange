package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.ActivityLog;
import com.example.virtual_exchange.domain.ActivityType;
import com.example.virtual_exchange.repository.ActivityLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class ActivityLogService {

    private final ActivityLogRepository activityLogRepository;

    @Async
    public void saveActivityLog(Long userId, ActivityType activityType, String description, String ip) {
        ActivityLog activityLog = ActivityLog.builder()
                .userId(userId)
                .activityType(activityType)
                .description(description)
                .ip(ip)
                .timestamp(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .build();

        activityLogRepository.save(activityLog);
    }
}
