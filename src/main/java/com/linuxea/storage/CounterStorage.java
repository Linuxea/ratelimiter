package com.linuxea.storage;

public interface CounterStorage {

  void init(int initValue);

  void set(int value);

  int getAndDecrement();

  int incrementAndGet();

  int get();

  boolean compareAndSet(int expect, int update);

}
