package com.linuxea.storage;

import redis.clients.jedis.Jedis;

public class RedisIntegerCounterStorage implements CounterStorage {

  private final String keyName;
  private final Jedis jedis;
  private final String lockKey;

  public RedisIntegerCounterStorage(String keyName, Jedis jedis) {
    this.keyName = keyName + ":counter";
    this.jedis = jedis;
    lockKey = keyName + ":lock";
  }

  @Override
  public void init(int initValue) {
    this.jedis.set(keyName, String.valueOf(initValue));
  }

  @Override
  public void set(int value) {
    this.jedis.set(keyName, String.valueOf(value));
  }

  @Override
  public int getAndDecrement() {
    while (!(this.jedis.setnx(lockKey, "1") > 1)) {
      // do nothing
    }
    int counterInt = this.get();
    if (counterInt > 0) {
      this.jedis.set(keyName, String.valueOf(counterInt - 1));
    }
    return counterInt;
  }

  @Override
  public int incrementAndGet() {
    while (!(this.jedis.setnx(lockKey, "1") > 1)) {
      // do nothing
    }

    final int[] counterInt = new int[1];
    new DelKeyCallback(this.jedis, lockKey) {
      @Override
      void doCore() {
        counterInt[0] = get();
        jedis.set(keyName, String.valueOf(counterInt[0] + 1));
      }
    }.doCall();
    return counterInt[0];
  }

  @Override
  public int get() {
    String counter = this.jedis.get(keyName);
    if (counter == null) {
      return 0;
    }
    return Integer.parseInt(counter);
  }

  @Override
  public boolean compareAndSet(int expect, int update) {
    while (!(this.jedis.setnx(lockKey, "1") > 1)) {
      // do nothing
    }

    final Boolean[] result = new Boolean[1];
    new DelKeyCallback(this.jedis, lockKey) {
      @Override
      void doCore() {
        int oldValue = get();
        if (oldValue == expect) {
          jedis.set(keyName, String.valueOf(update));
          result[0] = true;
        }
        result[0] = false;
      }
    }.doCall();
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
