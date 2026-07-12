package com.tandiantong.adminapi.platform;

import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platform/v1")
public class PlatformHealthController {

    @GetMapping("/health")
    public PlatformHealthResponse health() {
        return new PlatformHealthResponse("平台服务正常", Instant.now().toString());
    }

    public record PlatformHealthResponse(String status, String checkedAt) {
    }
}
