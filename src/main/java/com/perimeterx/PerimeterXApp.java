package com.perimeterx;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.ComponentScan;

import java.util.Arrays;

@SpringBootApplication()
@ComponentScan(basePackages = {
        "com.perimeterx.config",
        "com.perimeterx.controllers",
        "com.perimeterx.services"
})
public class PerimeterXApp extends SpringBootServletInitializer {
    public static void main(String[] args) {
        Arrays.stream(args).forEach(System.out::println);
        SpringApplication.run(PerimeterXApp.class, args);
    }
}