package com.linuxea;

/**
 * 限流器
 * <p> 统一限流接口
 * Created by Linuxea on 2019-04-28 22:10
 */
public interface RateLimiter {


  /**
   * 尝试获取令牌
   *
   * @return true:获取成功 false:获取失败
   */
  boolean tryAcquire();

  /**
   * 获取下一个窗口开始时间
   *
   * @return 下一个窗口开始时间
   */
  Long getNextWindowStartTimestamp();
}
