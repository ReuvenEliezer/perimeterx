package com.perimeterx.services;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

@Service
public class RateLimiterServiceImpl implements RateLimiterService {

    private static final Logger logger = LogManager.getLogger(RateLimiterServiceImpl.class);
    private static Map<Integer, Queue<LocalDateTime>> hashUrlReqToTimeMap = new ConcurrentHashMap<>();
    private static Duration timeIntervalBetweenRequests;
    private static long totalAllowReqInIntervalTime;

    public RateLimiterServiceImpl(ApplicationArguments applicationArgument) {
        setParams(applicationArgument);
    }

    @Override
    public boolean isReachedLimitation(String url) {
        LocalDateTime now = LocalDateTime.now();
        //BlockingQueue is thread safe
        Queue<LocalDateTime> lastReqTimeQueue = hashUrlReqToTimeMap.computeIfAbsent(url.hashCode(), q -> new LinkedBlockingQueue<>());
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

}
