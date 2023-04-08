package com.linuxea.impl;

import com.linuxea.RateLimiter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbsRateLimiter implements RateLimiter {

  protected int maxTokens;
  protected AtomicInteger tokens;
  protected long windowSize;
  protected TimeUnit windowTimeUnit;
  protected long delayTimeStamp;


  /**
   * 下一个窗口开始时间
   *
   * @return 下一个窗口开始时间
   */
  @Override
  public Long getNextWindowStartTimestamp() {
    return System.currentTimeMillis() + delayTimeStamp;
  }

}
