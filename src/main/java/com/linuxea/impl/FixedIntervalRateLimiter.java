package com.linuxea.impl;

import com.linuxea.RateLimiter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 固定时间间隔限流器
 */
public class FixedIntervalRateLimiter implements RateLimiter {

  private final int maxTokens;
  private final AtomicInteger tokens;

  public FixedIntervalRateLimiter(int maxTokens, long windowSize, TimeUnit timeUnit,
      ScheduledExecutorService scheduler) {
    this.maxTokens = maxTokens;
    this.tokens = new AtomicInteger(maxTokens);
    long windowSizeInMillis = timeUnit.toMillis(windowSize);
    long intervalInMillis = windowSizeInMillis / maxTokens;
    scheduler.scheduleAtFixedRate(this::addToken, intervalInMillis, intervalInMillis,
        TimeUnit.MILLISECONDS);
  }

  private void addToken() {
    int currentTokens;
    do {
      currentTokens = tokens.get();
      if (currentTokens >= maxTokens) {
        return;
      }
    } while (!tokens.compareAndSet(currentTokens, currentTokens + 1));
  }


  @Override
  public boolean tryAcquire() {
    int currentTokens = tokens.getAndDecrement();
    if (currentTokens > 0) {
      return true;
    } else {
      // 如果 tokens 变为负数，将其值恢复为0
      tokens.incrementAndGet();
      return false;
    }
  }

}
