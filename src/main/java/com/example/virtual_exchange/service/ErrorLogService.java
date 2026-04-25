package com.example.virtual_exchange.service;

import com.example.virtual_exchange.domain.ErrorLog;
import com.example.virtual_exchange.repository.ErrorLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ErrorLogService {

    private final ErrorLogRepository errorLogRepository;

    @Async
    public void saveErrorLog(Exception ex, HttpServletRequest request) {
        String stackTrace = Arrays.stream(ex.getStackTrace())
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n"));

        ErrorLog errorLog = ErrorLog.builder()
                .timestamp(LocalDateTime.now(ZoneId.of("Asia/Seoul")))
                .exceptionType(ex.getClass().getSimpleName())
                .message(ex.getMessage())
                .endpoint(request.getMethod() + " " + request.getRequestURI())
                .stackTrace(stackTrace)
                .build();

        errorLogRepository.save(errorLog);
    }
}
