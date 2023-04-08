package com.linuxea.impl;

import com.linuxea.RateLimiter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class FixWindowRateLimiterTest {

  @Test
  void acquire() {
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    RateLimiter rateLimiter = new FixWindowRateLimiter(1300, 1, TimeUnit.SECONDS,
        scheduledExecutorService);

    Integer success = 0;
    for (int i = 0; i < 500; i++) {
      if (rateLimiter.tryAcquire()) {
        System.out.println("success");
        success++;
      } else {
        System.out.println("fail");
      }
    }

    System.out.println("成功次数" + success);
    scheduledExecutorService.shutdown();

  }

}