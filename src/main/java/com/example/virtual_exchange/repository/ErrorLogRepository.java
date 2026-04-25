package com.example.virtual_exchange.repository;

import com.example.virtual_exchange.domain.ErrorLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ErrorLogRepository extends MongoRepository<ErrorLog, String> {
}
