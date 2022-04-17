package com.perimeterx.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor //for serializer
@Getter
@Setter
public class UrlRequest {
    private String url;
}
