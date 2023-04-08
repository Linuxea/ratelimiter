package com.linuxea.impl;

import com.linuxea.RateLimiter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FixWindowRateLimiter implements RateLimiter {

  private final int maxTokens;
  private final AtomicInteger tokens;

  public FixWindowRateLimiter(int maxTokens, long windowSize, TimeUnit timeUnit,
      ScheduledExecutorService scheduler) {
    this.maxTokens = maxTokens;
    this.tokens = new AtomicInteger(maxTokens);
    long windowSizeInMillis = timeUnit.toMillis(windowSize);
    scheduler.scheduleAtFixedRate(this::addTokens, windowSizeInMillis, windowSizeInMillis,
        TimeUnit.MILLISECONDS);
  }


  private void addTokens() {
    tokens.set(Math.min(tokens.get() + maxTokens, maxTokens));
  }

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

