package org.example.modules.checkout.repository;

import org.example.modules.checkout.model.WebhookEventLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEventLogRepository extends JpaRepository<WebhookEventLog, Long> {
    boolean existsByEventId(String eventId);
}
