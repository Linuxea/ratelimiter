package com.linuxea.impl;

import com.linuxea.storage.CounterStorage;

public abstract class AbsCounterRateLimiter extends AbsRateLimiter {

  public CounterStorage tokens;

}
