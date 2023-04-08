package com.linuxea.impl;

import com.linuxea.storage.AtomicIntegerCounterStorage;
import com.linuxea.storage.CounterStorage;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FixWindowRateLimiter extends AbsCounterRateLimiter {

  public FixWindowRateLimiter(int maxTokens, long windowSize, Long windowStart,
      TimeUnit windowTimeUnit, ScheduledExecutorService scheduler, CounterStorage tokens) {
    this.maxTokens = maxTokens;
    tokens.init(windowStart != null && System.currentTimeMillis() > windowStart ? maxTokens : 0);
    this.tokens = tokens;
    this.windowSize = windowSize;
    this.windowTimeUnit = windowTimeUnit;
    long windowSizeInMillis = windowTimeUnit.toMillis(windowSize);
    this.delayTimeStamp = calculateDelay(windowStart, windowSizeInMillis);
    scheduler.scheduleAtFixedRate(this::addTokens, delayTimeStamp, windowSizeInMillis,
        TimeUnit.MILLISECONDS);
  }

  public FixWindowRateLimiter(int maxTokens, long windowSize, Long windowStart,
      TimeUnit windowTimeUnit, ScheduledExecutorService scheduler) {
    this(maxTokens, windowSize, windowStart, windowTimeUnit, scheduler,
        new AtomicIntegerCounterStorage());
  }

  public FixWindowRateLimiter(int maxTokens, long windowSize, TimeUnit windowTimeUnit,
      ScheduledExecutorService scheduler, CounterStorage tokens) {
    this.maxTokens = maxTokens;
    tokens.init(0);
    this.tokens = tokens;
    this.windowSize = windowSize;
    this.windowTimeUnit = windowTimeUnit;
    long windowSizeInMillis = windowTimeUnit.toMillis(windowSize);
    scheduler.scheduleAtFixedRate(this::addTokens, 0, windowSizeInMillis, TimeUnit.MILLISECONDS);
  }

  public FixWindowRateLimiter(int maxTokens, long windowSize, TimeUnit windowTimeUnit,
      ScheduledExecutorService scheduler) {
    this(maxTokens, windowSize, null, windowTimeUnit, scheduler, new AtomicIntegerCounterStorage());
  }

  /**
   * 计算延迟时间
   *
   * @param windowStartMillis 从什么时候开始计算
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
    tokens.set(maxTokens);
  }

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

