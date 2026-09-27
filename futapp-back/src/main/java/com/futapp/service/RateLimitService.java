package com.futapp.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimitService {
    public record Decision(boolean allowed, int remaining, long retryAfterSeconds) {}
    private record Bucket(int count, Instant expiresAt) {}
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Decision consume(String key, int limit, Duration window) {
        Instant now = Instant.now();
        Decision[] result = new Decision[1];
        buckets.compute(key, (ignored, current) -> {
            if (current == null || !current.expiresAt().isAfter(now)) {
                result[0] = new Decision(true, limit - 1, window.toSeconds());
                return new Bucket(1, now.plus(window));
            }
            long retry = Math.max(1, Duration.between(now, current.expiresAt()).toSeconds());
            if (current.count() >= limit) {
                result[0] = new Decision(false, 0, retry);
                return current;
            }
            int count = current.count() + 1;
            result[0] = new Decision(true, limit - count, retry);
            return new Bucket(count, current.expiresAt());
        });
        return result[0];
    }

    @Scheduled(cron = "${futapp.maintenance.rate-limit-cleanup-cron:0 */10 * * * *}")
    public void limpiarVencidos() {
        Instant now = Instant.now();
        buckets.entrySet().removeIf(entry -> !entry.getValue().expiresAt().isAfter(now));
    }
}
