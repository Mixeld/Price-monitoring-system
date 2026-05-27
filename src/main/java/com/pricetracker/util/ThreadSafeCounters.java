package com.pricetracker.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ThreadSafeCounters {

  public static class AtomicCounter {
    private final AtomicInteger count = new AtomicInteger(0);

    public void increment() {
      count.incrementAndGet();
    }

    public int getCount() {
      return count.get();
    }

    public void reset() {
      count.set(0);
    }
  }

  public static class AtomicLongCounter {
    private final AtomicLong count = new AtomicLong(0);

    public void increment() {
      count.incrementAndGet();
    }

    public long getCount() {
      return count.get();
    }

    public void reset() {
      count.set(0);
    }
  }

  public static class SynchronizedCounter {
    private int count = 0;

    public synchronized void increment() {
      count++;
    }

    public synchronized int getCount() {
      return count;
    }

    public synchronized void reset() {
      count = 0;
    }
  }

  public static class UnsafeCounter {
    private int count = 0;

    public void increment() {
      count++;  // race condition здесь!
    }

    public int getCount() {
      return count;
    }

    public void reset() {
      count = 0;
    }
  }
}