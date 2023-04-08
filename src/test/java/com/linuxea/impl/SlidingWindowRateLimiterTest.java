package com.linuxea.impl;

import com.linuxea.RateLimiter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

class SlidingWindowRateLimiterTest {


  @Test
  @SuppressWarnings("deprecation")
  public void testRateLimiter() {

    Jedis jedis = new Jedis(System.getenv("REDIS_HOST"));

    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    RateLimiter rateLimiter = new SlidingWindowRateLimiter(200, jedis, TimeUnit.SECONDS, 1,
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
    jedis.close();
  }

}