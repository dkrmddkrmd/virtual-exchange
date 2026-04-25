package com.example.virtual_exchange.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "error_logs")
public class ErrorLog {
    @Id
    private String id;
    @Indexed(expireAfter = "30d")
    private LocalDateTime timestamp;
    private String exceptionType;
    private String message;
    private String endpoint;
    private String stackTrace;
}
