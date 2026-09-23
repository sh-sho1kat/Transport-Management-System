package com.tms.shared.web;

import com.tms.shared.error.ApiException;
import java.time.Clock;
import java.util.*;
import org.springframework.stereotype.Component;

/** Bounded single-instance limiter. Use a shared edge limiter when running multiple replicas. */
@Component
public class RateLimiter {
  private record Bucket(long window, int count) {}

  private final Map<String, Bucket> buckets = new HashMap<>();
  private final Clock clock;

  public RateLimiter(Clock clock) {
    this.clock = clock;
  }

  public synchronized void check(String key, int limit) {
    long now = clock.millis() / 60000;
    buckets.entrySet().removeIf(e -> e.getValue().window() < now);
    var b = buckets.getOrDefault(key, new Bucket(now, 0));
    if (b.count() >= limit || (!buckets.containsKey(key) && buckets.size() >= 10000))
      throw new ApiException(429, "RATE_LIMITED", "Too many requests. Try again in a minute");
    buckets.put(key, new Bucket(now, b.count() + 1));
  }
}
