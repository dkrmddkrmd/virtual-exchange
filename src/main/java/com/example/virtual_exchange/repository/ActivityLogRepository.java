package com.example.virtual_exchange.repository;

import com.example.virtual_exchange.domain.ActivityLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ActivityLogRepository extends MongoRepository<ActivityLog, String> {
}
