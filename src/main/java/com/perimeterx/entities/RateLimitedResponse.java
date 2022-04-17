package com.perimeterx.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor //for serializer
@Getter
public class RateLimitedResponse {
    private boolean block;
}
