package com.tandiantong.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 摊点通模块化单体应用启动入口。
 */
@SpringBootApplication(scanBasePackages = {
        "com.tandiantong.bootstrap",
        "com.tandiantong.adminapi",
        "com.tandiantong.miniapi",
        "com.tandiantong.security",
        "com.tandiantong.catalog",
        "com.tandiantong.order",
        "com.tandiantong.reservation",
        "com.tandiantong.verification",
        "com.tandiantong.analytics",
        "com.tandiantong.integration"
})
public class TandianTongApplication {

    public static void main(String[] args) {
        SpringApplication.run(TandianTongApplication.class, args);
    }
}
