package com.perimeterx.services;


public interface RateLimiterService {
    boolean isReachedLimitation(String url);
}