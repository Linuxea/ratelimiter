package com.linuxea.impl;

import com.linuxea.RateLimiter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class ApiRateLimiterTest {

  @Test
  public void test() throws InterruptedException {
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    int maxApiRequest = 50;
    int windowSize = 10;
    //每10秒钟最多允许50个请求, 每秒钟最多允许5个请求
    RateLimiter rateLimiter = new FixedIntervalRateLimiter(maxApiRequest, windowSize,
        TimeUnit.SECONDS,
        scheduledExecutorService);

    Long nextWindowStartTimestamp = rateLimiter.getNextWindowStartTimestamp();
    System.out.println("下一个窗口开始时间:" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
        new Date(nextWindowStartTimestamp)));

    //token 资源创建预热
    TimeUnit.MILLISECONDS.sleep(1000);

    Integer successCount = 0;
    for (int j = 0; j < 10; j++) {
      for (int i = 0; i < 10; i++) {
        boolean canShopping = rateLimiter.tryAcquire();
        if (canShopping) {
          successCount++;
          System.out.println("请求成功");
        } else {
          System.out.println("请求失败");
        }
      }
      TimeUnit.SECONDS.sleep(1);
    }

    System.out.println("请求成功次数:" + successCount);

  }

}
