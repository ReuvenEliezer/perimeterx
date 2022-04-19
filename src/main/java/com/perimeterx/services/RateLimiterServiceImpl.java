package com.perimeterx.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

@Service
public class RateLimiterServiceImpl implements RateLimiterService {

    private static final Logger logger = LogManager.getLogger(RateLimiterServiceImpl.class);
    private static Map<String, LinkedBlockingDeque<LocalDateTime>> hashUrlReqToTimeMap = new ConcurrentHashMap<>();
    private static Duration timeIntervalBetweenRequests;
    private static long totalAllowReqInIntervalTime;
    private ScheduledFuture<?> cleanOldRequestsMapFuture = null;

    @Autowired
    private PerimeterScheduler perimeterScheduler;

    public RateLimiterServiceImpl(ApplicationArguments applicationArgument) {
        setParams(applicationArgument);
    }


    @PostConstruct()
    private void initCleanOldRequestsFromMap() {
        if (cleanOldRequestsMapFuture == null) {
            cleanOldRequestsMapFuture = perimeterScheduler.scheduleWithFixedDelay(this::cleanOldRequestsFromMap,
                    timeIntervalBetweenRequests.toMillis(), timeIntervalBetweenRequests.toMillis(), TimeUnit.MILLISECONDS);
            //TODO * 10
        }
    }

    private void cleanOldRequestsFromMap() {
        logger.info("clearMap");
        hashUrlReqToTimeMap.forEach((key, value) -> {
            if (value == null || value.isEmpty() || value.getLast().plusSeconds(timeIntervalBetweenRequests.getSeconds()).isBefore(LocalDateTime.now())) {
                logger.info("remove key=[{}] with values size={} because of the last req is from {}. (passed timeIntervalBetweenRequests {})", key, value.size(), value.getLast(), timeIntervalBetweenRequests);
                hashUrlReqToTimeMap.remove(key);
            }
        });
    }

    @Override
    public boolean isReachedLimitation(String url) {
        LocalDateTime now = LocalDateTime.now();
        logger.info("url={}", url);
        /**
         * https://stackoverflow.com/questions/3362018/is-linkedlist-thread-safe-when-im-accessing-it-with-offer-and-poll-exclusively
         //BlockingQueue is thread safe
         */
        Queue<LocalDateTime> lastReqTimeQueue = hashUrlReqToTimeMap.computeIfAbsent(url, q -> new LinkedBlockingDeque<>());
        boolean isReachedLimitation = isReachedLimitation(now, lastReqTimeQueue);
        lastReqTimeQueue.add(now);

        //for multi threaded         //TODO remove while statement
        while (lastReqTimeQueue.size() > totalAllowReqInIntervalTime) {
            logger.debug("remove: {}", lastReqTimeQueue.remove());
        }
        return isReachedLimitation;
    }

    private boolean isReachedLimitation(LocalDateTime now, Queue<LocalDateTime> lastReqTimeQueue) {
        if (lastReqTimeQueue.size() >= totalAllowReqInIntervalTime
                && Duration.between(lastReqTimeQueue.remove(), now).minus(timeIntervalBetweenRequests).isNegative()) {
            return true;
        }
        return false;
    }

    private void setParams(ApplicationArguments applicationArgument) {
        List<String> nonOptionArgs = applicationArgument.getNonOptionArgs();
        if (nonOptionArgs.size() < 2) {
            throw new IllegalArgumentException("missing app args [timeIntervalBetweenRequestsInMillis, totalAllowRequestsInIntervalTime]");
        }

        try {
            long timeIntervalBetweenRequestsInMillis = Long.parseLong(nonOptionArgs.get(0));
            this.timeIntervalBetweenRequests = Duration.ofMillis(timeIntervalBetweenRequestsInMillis);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("the first main arg: [timeIntervalBetweenRequests] must be a long value", e);
        }

        try {
            this.totalAllowReqInIntervalTime = Long.parseLong(nonOptionArgs.get(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("the second arg [totalAllowReqInIntervalTime] must be a long value", e);
        }
        logger.info("run app with timeIntervalBetweenRequests=[{}], totalAllowReqInIntervalTime[{}]", timeIntervalBetweenRequests, totalAllowReqInIntervalTime);
    }

    @PreDestroy
    private final void stop() {
        logger.info("stopping");
        if (cleanOldRequestsMapFuture != null)
            cleanOldRequestsMapFuture.cancel(false);
        cleanOldRequestsMapFuture = null;
    }

}
