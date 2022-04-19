package com.perimeterx.services;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface PerimeterScheduler {
    ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long initialDelay, long delay, TimeUnit unit);

}
