package com.linuxea.impl;

import com.linuxea.RateLimiter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class FixWindowRateLimiter implements RateLimiter {

  private final int maxTokens;
  private final AtomicInteger tokens;

  public FixWindowRateLimiter(int maxTokens, long windowSize, Long windowStart, TimeUnit timeUnit,
      ScheduledExecutorService scheduler) {
    this.maxTokens = maxTokens;
    this.tokens = new AtomicInteger(0);
    long windowSizeInMillis = timeUnit.toMillis(windowSize);
    long delay = calculateDelay(windowStart, windowSizeInMillis);
    scheduler.scheduleAtFixedRate(this::addTokens, delay, windowSizeInMillis,
        TimeUnit.MILLISECONDS);
  }

  public FixWindowRateLimiter(int maxTokens, long windowSize, TimeUnit timeUnit,
      ScheduledExecutorService scheduler) {
    this(maxTokens, windowSize, null, timeUnit, scheduler);
  }

  /**
   * 计算延迟时间
   *
   * @param windowStartMillis  从什么时候开始计算
   * @param windowSizeInMillis 窗口时间大小
   * @return 延迟时间
   */
  private long calculateDelay(Long windowStartMillis, long windowSizeInMillis) {
    long ctMillis = System.currentTimeMillis();
    if (windowStartMillis == null) {
      return 0;
    } else if (windowStartMillis <= ctMillis) {
      //计算从上一个窗口开始到当前时间的已过去的毫秒数。
      long elapsedTimeSinceWindowStart = (ctMillis - windowStartMillis) % windowSizeInMillis;
      //计算上一个窗口还需要执行的时间
      return windowSizeInMillis - elapsedTimeSinceWindowStart;
    } else {
      return windowStartMillis - ctMillis;
    }
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

