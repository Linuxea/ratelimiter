package com.linuxea.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class FixWindowRateLimiterDelayTest {

  @Test
  void testWindowStartAfterCurrentTime() throws InterruptedException {
    // 获取当前时间戳，再加上 2 秒作为窗口开始时间
    long windowStartMillis = System.currentTimeMillis() + 2000;
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    FixWindowRateLimiter rateLimiter = new FixWindowRateLimiter(10, 5, windowStartMillis,
        TimeUnit.SECONDS, scheduler);
    assertFalse(rateLimiter.tryAcquire());

    // 等待 2.5 秒，此时窗口已经开始，应该可以获取令牌
    Thread.sleep(2500);
    assertTrue(rateLimiter.tryAcquire());
    scheduler.shutdown();
  }

  @Test
  void testWindowStartBeforeCurrentTime() throws InterruptedException {
    // 获取当前时间戳，再减去 2 秒作为窗口开始时间
    long windowStartMillis = System.currentTimeMillis() - 2000;
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    FixWindowRateLimiter rateLimiter = new FixWindowRateLimiter(10, 5, windowStartMillis,
        TimeUnit.SECONDS, scheduler);
    assertFalse(rateLimiter.tryAcquire());

    TimeUnit.MILLISECONDS.sleep(3500);
    // 获取所有剩余的令牌
    for (int i = 0; i < 10; i++) {
      assertTrue(rateLimiter.tryAcquire());
    }

    // 令牌已用完，应该返回 false
    assertFalse(rateLimiter.tryAcquire());

    scheduler.shutdown();
  }

}

