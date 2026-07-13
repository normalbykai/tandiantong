package com.tandiantong.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 摊点通模块化单体应用启动入口。
 */
@SpringBootApplication(scanBasePackages = "com.tandiantong")
public class TandianTongApplication {

    public static void main(String[] args) {
        SpringApplication.run(TandianTongApplication.class, args);
    }
}
