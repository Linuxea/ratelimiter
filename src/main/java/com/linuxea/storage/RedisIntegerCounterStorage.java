package com.linuxea.storage;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisIntegerCounterStorage implements CounterStorage {

  private final String keyName;
  private final JedisPool jedisPool;
  private final String lockKey;

  public RedisIntegerCounterStorage(String keyName, JedisPool jedisPool) {
    this.keyName = keyName + ":counter";
    this.jedisPool = jedisPool;
    lockKey = keyName + ":lock";
  }

  @Override
  public void init(int initValue) {
    Jedis resource = this.jedisPool.getResource();
    resource.set(keyName, String.valueOf(initValue));
    jedisPool.returnResource(resource);
  }

  @Override
  public void set(int value) {
    Jedis resource = this.jedisPool.getResource();
    resource.set(keyName, String.valueOf(value));
    jedisPool.returnResource(resource);
  }

  @Override
  public int getAndDecrement() {
    Jedis resource = this.jedisPool.getResource();
    while (true) {
      if (!(resource.setnx(lockKey, "1") > 1)) {
        break;
      }
    }
    int counterInt = this.get();
    if (counterInt > 0) {
      resource.set(keyName, String.valueOf(counterInt - 1));
    }
    resource.del(lockKey);
    jedisPool.returnResource(resource);
    return counterInt;
  }

  @Override
  public int incrementAndGet() {
    Jedis resource = this.jedisPool.getResource();
    while (true) {
      if (!(resource.setnx(lockKey, "1") > 1)) {
        break;
      }
    }

    final int[] counterInt = new int[1];
    new DelKeyCallback(resource, lockKey) {
      @Override
      void doCore() {
        counterInt[0] = get();
        resource.set(keyName, String.valueOf(counterInt[0] + 1));
      }
    }.doCall();
    jedisPool.returnResource(resource);
    return counterInt[0];
  }

  @Override
  public int get() {
    Jedis resource = this.jedisPool.getResource();
    String counter = resource.get(keyName);
    if (counter == null) {
      return 0;
    }
    jedisPool.returnResource(resource);
    return Integer.parseInt(counter);
  }

  @Override
  public boolean compareAndSet(int expect, int update) {
    Jedis resource = this.jedisPool.getResource();
    while (true) {
      if (!(resource.setnx(lockKey, "1") > 1)) {
        break;
      }
    }

    final Boolean[] result = new Boolean[1];
    new DelKeyCallback(resource, lockKey) {
      @Override
      void doCore() {
        int oldValue = get();
        if (oldValue == expect) {
          resource.set(keyName, String.valueOf(update));
          result[0] = true;
        }
        result[0] = false;
      }
    }.doCall();
    jedisPool.returnResource(resource);
    return result[0];
  }

  private static abstract class Callback {

    private final Runnable runnable;

    private Callback(Runnable runnable) {
      this.runnable = runnable;
    }

    abstract void doCore();

    public void doCall() {
      doCore();
      runnable.run();
    }
  }

  private abstract static class DelKeyCallback extends Callback {

    private DelKeyCallback(Jedis jedis, String lockKey) {
      super(() -> jedis.del(lockKey));
    }
  }


}
