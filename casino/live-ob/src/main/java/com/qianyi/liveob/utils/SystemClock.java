package com.qianyi.liveob.utils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class SystemClock {

    private static final String THREAD_NAME = "system.clock";

    private static final SystemClock MILLIS_CLOCK = new SystemClock(1);

    private final long precision;

    private final AtomicLong now;

    private SystemClock(long precision) {

        this.precision = precision;

        now = new AtomicLong(System.currentTimeMillis());

        scheduleClockUpdating();

    }

    public static SystemClock millisClock() {

        return MILLIS_CLOCK;

    }

    private void scheduleClockUpdating() {

        ScheduledExecutorService scheduler =

                Executors.newSingleThreadScheduledExecutor(runnable -> {

                    Thread thread = new Thread(runnable, THREAD_NAME);

                    thread.setDaemon(true);

                    thread.setPriority(Thread.MAX_PRIORITY); //设置线程优先级，让时间线程尽可能运行(依赖于操作系统的调度实现)

                    return thread;

                });

        scheduler.scheduleAtFixedRate(() ->

                now.set(System.currentTimeMillis()), precision, precision, TimeUnit.MILLISECONDS);

    }

    public long now() {

        return now.get();

    }
}
