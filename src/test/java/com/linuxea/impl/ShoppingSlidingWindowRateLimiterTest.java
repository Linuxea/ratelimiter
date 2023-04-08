package com.linuxea.impl;

import static org.junit.jupiter.api.Assertions.assertFalse;

import com.linuxea.RateLimiter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 在一个在线购物网站中，为了防止刷单行为，限制每个用户在30分钟内只能下单数量为1件
 */
public class ShoppingSlidingWindowRateLimiterTest {

  @Test
  public void test() throws InterruptedException {
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    int maxShoppTimes = 100;
    int windowSize = 30;
    JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), System.getenv("REDIS_HOST"), 6379);
    RateLimiter rateLimiter = new SlidingWindowRateLimiter(maxShoppTimes, jedisPool, windowSize,
        TimeUnit.MINUTES, scheduledExecutorService);

    Long nextWindowStartTimestamp = rateLimiter.getNextWindowStartTimestamp();
    System.out.println("下一个窗口开始时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
        new Date(nextWindowStartTimestamp)));

    //token 资源创建预热
    TimeUnit.MILLISECONDS.sleep(200);
    Integer successCount = 0;
    Integer failCount = 0;
    for (int i = 0; i < 110; i++) {
      boolean canShopping = rateLimiter.tryAcquire();
      if (canShopping) {
        System.out.println("购买成功");
        successCount++;
      } else {
        System.out.println("购买失败");
        failCount++;
      }
    }
    System.out.println("购买成功次数:" + successCount);
    System.out.println("购买失败次数:" + failCount);

    TimeUnit.SECONDS.sleep(2);
    assertFalse(rateLimiter.tryAcquire());


  }

}
