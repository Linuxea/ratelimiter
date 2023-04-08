package com.linuxea.impl;

import com.linuxea.RateLimiter;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

public class NaturalSignRateLimiterTest {

  @Test
  public void test() throws InterruptedException {
    ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    LocalDate today = LocalDate.now();
    LocalDateTime todayStart = LocalDateTime.of(today, LocalTime.MIN);
    // 获取今天零点的时间戳
    long zero = todayStart.toEpochSecond(ZoneOffset.of("+8")) * 1000;

    System.out.println(
        "上一次窗口开始时间" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
            new Date(zero)));

    RateLimiter rateLimiter = new FixWindowRateLimiter(1, 1, zero,
        TimeUnit.DAYS, scheduledExecutorService);

    Long nextWindowStartTimestamp = rateLimiter.getNextWindowStartTimestamp();
    System.out.println(
        "下一次窗口开始时间" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
            new Date(nextWindowStartTimestamp)));

    TimeUnit.SECONDS.sleep(1);

    for (int i = 0; i < 10; i++) {
      System.out.println("能否签到" + rateLimiter.tryAcquire());
    }

    scheduledExecutorService.shutdown();


  }

}
