package com.linuxea.impl;

import com.linuxea.storage.AtomicIntegerCounterStorage;
import com.linuxea.storage.CounterStorage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 固定时间间隔限流器
 */
public class FixedIntervalRateLimiter extends AbsCounterRateLimiter {

  public FixedIntervalRateLimiter(int maxTokens, long windowSize, TimeUnit windowTimeUnit,
      ScheduledExecutorService scheduler) {
    this(maxTokens, windowSize, windowTimeUnit, scheduler, new AtomicIntegerCounterStorage());
  }

  public FixedIntervalRateLimiter(int maxTokens, long windowSize, TimeUnit windowTimeUnit,
      ScheduledExecutorService scheduler, CounterStorage tokens) {
    this.maxTokens = maxTokens;
    tokens.init(0);
    this.tokens = tokens;
    this.windowSize = windowSize;
    this.windowTimeUnit = windowTimeUnit;
    long windowSizeInMillis = windowTimeUnit.toMillis(windowSize);
    long intervalInMillis = windowSizeInMillis / maxTokens;
    this.delayTimeStamp = intervalInMillis;
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
    } else if (currentTokens < 0) {
      // 如果 tokens 变为负数，将其值恢复为0
      tokens.incrementAndGet();
    }
    return false;
  }

}
