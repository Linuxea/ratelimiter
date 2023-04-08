package com.linuxea.impl;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class SlidingWindowRateLimiter extends AbsRateLimiter {

  private final JedisPool jedisPool;
  private final String key;

  public SlidingWindowRateLimiter(int maxTokens, JedisPool jedisPool,
      Integer windowSize, TimeUnit windowTimeUnit, ScheduledExecutorService scheduler) {
    this.maxTokens = maxTokens;
    this.jedisPool = jedisPool;
    this.windowSize = windowSize;
    this.windowTimeUnit = windowTimeUnit;
    //identifier for the sorted set
    this.key = "sliding_window" + UUID.randomUUID();
    // Schedule a task to remove events older than the lower bound from the sorted set
    long windowSizeInMillis = windowTimeUnit.toMillis(windowSize);
    this.delayTimeStamp = windowSizeInMillis;
    scheduler.scheduleAtFixedRate(this::removeOlderEventOutOfWindows, windowSizeInMillis,
        windowSizeInMillis, TimeUnit.MILLISECONDS);
  }

  @Override
  public boolean tryAcquire() {
    Jedis jedis = jedisPool.getResource();
    long tsSeconds = Instant.now().getEpochSecond();

    // Add the event to the sorted set
    jedis.zadd(key, tsSeconds, UUID.randomUUID().toString());

    // Calculate the lower and upper bounds for the sliding window
    long lowerBound = tsSeconds - windowTimeUnit.toSeconds(windowSize);

    // Count the events in the sliding window
    long eventCount = jedis.zcount(key, String.valueOf(lowerBound + 1), String.valueOf(tsSeconds));

    jedisPool.returnResource(jedis);
    return eventCount <= maxTokens;
  }

  private void removeOlderEventOutOfWindows() {
    Jedis jedis = jedisPool.getResource();
    long tsSeconds = Instant.now().getEpochSecond();
    long lowerBound = tsSeconds - windowTimeUnit.toSeconds(windowSize);
    // Remove events older than the lower bound from the sorted set
    jedis.zremrangeByScore(key, "-inf", String.valueOf(lowerBound));
    jedisPool.returnResource(jedis);
  }

}
