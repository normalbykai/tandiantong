package com.tandiantong.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.tandiantong")
public class TandianTongApplication {

    public static void main(String[] args) {
        SpringApplication.run(TandianTongApplication.class, args);
    }
}
