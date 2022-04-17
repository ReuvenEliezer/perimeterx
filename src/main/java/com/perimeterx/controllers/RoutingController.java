package com.perimeterx.controllers;

import com.perimeterx.entities.RateLimitedResponse;
import com.perimeterx.entities.UrlRequest;
import com.perimeterx.services.RateLimiterService;
import com.perimeterx.utils.WsAddressConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(WsAddressConstants.perimeterXLogicUrl)
public class RoutingController {

    @Autowired
    private RateLimiterService rateLimiterService;

    @PostMapping()
    public RateLimitedResponse isReachedLimitation(@RequestBody UrlRequest urlRequest) {
        return new RateLimitedResponse(rateLimiterService.isReachedLimitation(urlRequest.getUrl()));
    }

}
