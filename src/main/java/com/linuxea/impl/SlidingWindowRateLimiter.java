package com.linuxea.impl;

import com.linuxea.RateLimiter;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import redis.clients.jedis.Jedis;

public class SlidingWindowRateLimiter implements RateLimiter {

  private final int maxTokens;
  private final Jedis jedis;
  private final Integer windowSize;
  private final TimeUnit widowSizeUnit;
  private final String key;

  public SlidingWindowRateLimiter(int maxTokens, Jedis jedis, TimeUnit widowSizeUnit,
      Integer windowSize, ScheduledExecutorService scheduler) {
    this.maxTokens = maxTokens;
    this.jedis = jedis;
    this.windowSize = windowSize;
    this.widowSizeUnit = widowSizeUnit;
    //identifier for the sorted set
    this.key = "sliding_window" + UUID.randomUUID();
    // Schedule a task to remove events older than the lower bound from the sorted set
    long windowSizeInMillis = widowSizeUnit.toMillis(windowSize);
    scheduler.scheduleAtFixedRate(this::removeOlderEventOutOfWindows, windowSizeInMillis,
        windowSizeInMillis,
        TimeUnit.MILLISECONDS);
  }

  @Override
  public boolean tryAcquire() {
    long tsSeconds = Instant.now().getEpochSecond();

    // Add the event to the sorted set
    jedis.zadd(key, tsSeconds, UUID.randomUUID().toString());

    // Calculate the lower and upper bounds for the sliding window
    long lowerBound = tsSeconds - widowSizeUnit.toSeconds(windowSize);

    // Count the events in the sliding window
    long eventCount = jedis.zcount(key, String.valueOf(lowerBound + 1), String.valueOf(tsSeconds));

    return eventCount <= maxTokens;
  }

  private void removeOlderEventOutOfWindows() {
    long tsSeconds = Instant.now().getEpochSecond();
    long lowerBound = tsSeconds - widowSizeUnit.toSeconds(windowSize);
    // Remove events older than the lower bound from the sorted set
    jedis.zremrangeByScore(key, "-inf", String.valueOf(lowerBound));
  }

}
