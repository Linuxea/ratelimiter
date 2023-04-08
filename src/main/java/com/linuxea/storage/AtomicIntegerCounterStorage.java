package com.linuxea.storage;

public class AtomicIntegerCounterStorage implements CounterStorage {

  private java.util.concurrent.atomic.AtomicInteger counter;

  @Override
  public void init(int initValue) {
    counter = new java.util.concurrent.atomic.AtomicInteger(initValue);
  }

  @Override
  public void set(int value) {
    counter.set(value);
  }

  @Override
  public int getAndDecrement() {
    return counter.getAndDecrement();
  }

  @Override
  public int incrementAndGet() {
    return counter.incrementAndGet();
  }

  @Override
  public int get() {
    return counter.get();
  }

  @Override
  public boolean compareAndSet(int expect, int update) {
    return counter.compareAndSet(expect, update);
  }

}
