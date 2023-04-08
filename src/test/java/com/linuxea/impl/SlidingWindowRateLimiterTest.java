package com.linuxea.impl;

import com.linuxea.RateLimiter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class SlidingWindowRateLimiterTest {


  @Test
  public void testRateLimiter() {

    JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "REDIS_HOST", 6379);

    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    RateLimiter rateLimiter = new SlidingWindowRateLimiter(20, jedisPool, 3, TimeUnit.SECONDS,
        scheduledExecutorService);

    Integer success = 0;
    for (int i = 0; i < 40; i++) {
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