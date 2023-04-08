package com.linuxea.impl;

import com.linuxea.RateLimiter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class FixedIntervalRateLimiterTest {


  @Test
  public void test() throws InterruptedException {
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    RateLimiter fixedIntervalRateLimiter = new FixedIntervalRateLimiter(1000, 10,
        TimeUnit.SECONDS,
        scheduledExecutorService);
    TimeUnit.SECONDS.sleep(1);

    Integer success = 0;
    for (int i = 0; i < 500; i++) {
      if (fixedIntervalRateLimiter.tryAcquire()) {
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