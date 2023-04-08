package com.linuxea.impl;

import com.linuxea.RateLimiter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class FixWindowRateLimiterTest {

  @Test
  void acquire() throws InterruptedException {
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    RateLimiter rateLimiter = new FixWindowRateLimiter(20, 1, TimeUnit.SECONDS,
        scheduledExecutorService);

    Integer success = 0;
    for (int i = 0; i < 50; i++) {
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

  @Test
  void acquireDelay() throws InterruptedException {
    System.out.println("当前时间" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    RateLimiter rateLimiter = new FixWindowRateLimiter(20, 10,
        System.currentTimeMillis() - TimeUnit.SECONDS.toMillis(5), TimeUnit.SECONDS,
        scheduledExecutorService);

    Integer success = 0;
    for (int i = 0; i < 50; i++) {
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