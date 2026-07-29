package com.igou.mall.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {

    private static final int DEFAULT_RATE_LIMIT = 100;
    private static final int DEFAULT_TIME_WINDOW = 60000;

    private final ConcurrentHashMap<String, RateLimitEntry> rateLimits = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> blockedIPs = new ConcurrentHashMap<>();

    public boolean checkRateLimit(String key) {
        long now = System.currentTimeMillis();

        if (isIPBlocked(key)) {
            return false;
        }

        RateLimitEntry entry = rateLimits.computeIfAbsent(key, k -> new RateLimitEntry());

        synchronized (entry) {
            if (now - entry.getStartTime() > DEFAULT_TIME_WINDOW) {
                entry.reset();
            }

            if (entry.getCount().get() >= DEFAULT_RATE_LIMIT) {
                blockIP(key);
                return false;
            }

            entry.getCount().incrementAndGet();
            return true;
        }
    }

    public boolean checkRateLimit(String key, int limit, int timeWindowMs) {
        long now = System.currentTimeMillis();

        if (isIPBlocked(key)) {
            return false;
        }

        String compositeKey = key + ":" + limit + ":" + timeWindowMs;
        RateLimitEntry entry = rateLimits.computeIfAbsent(compositeKey, k -> new RateLimitEntry(timeWindowMs));

        synchronized (entry) {
            if (now - entry.getStartTime() > entry.getTimeWindow()) {
                entry.reset();
            }

            if (entry.getCount().get() >= limit) {
                blockIP(key);
                return false;
            }

            entry.getCount().incrementAndGet();
            return true;
        }
    }

    private boolean isIPBlocked(String key) {
        Long blockEndTime = blockedIPs.get(key);
        if (blockEndTime != null && blockEndTime > System.currentTimeMillis()) {
            return true;
        } else if (blockEndTime != null) {
            blockedIPs.remove(key);
        }
        return false;
    }

    private void blockIP(String key) {
        blockedIPs.put(key, System.currentTimeMillis() + 300000);
    }

    public int getRemaining(String key) {
        RateLimitEntry entry = rateLimits.get(key);
        if (entry == null) {
            return DEFAULT_RATE_LIMIT;
        }

        synchronized (entry) {
            if (System.currentTimeMillis() - entry.getStartTime() > DEFAULT_TIME_WINDOW) {
                return DEFAULT_RATE_LIMIT;
            }
            return Math.max(0, DEFAULT_RATE_LIMIT - entry.getCount().get());
        }
    }

    public long getBlockedIPCount() {
        return blockedIPs.size();
    }

    public void clearRateLimits() {
        rateLimits.clear();
        blockedIPs.clear();
    }

    private static class RateLimitEntry {
        private final AtomicInteger count = new AtomicInteger(0);
        private long startTime = System.currentTimeMillis();
        private final int timeWindow;

        public RateLimitEntry() {
            this(DEFAULT_TIME_WINDOW);
        }

        public RateLimitEntry(int timeWindow) {
            this.timeWindow = timeWindow;
        }

        public AtomicInteger getCount() {
            return count;
        }

        public long getStartTime() {
            return startTime;
        }

        public int getTimeWindow() {
            return timeWindow;
        }

        public void reset() {
            count.set(0);
            startTime = System.currentTimeMillis();
        }
    }
}