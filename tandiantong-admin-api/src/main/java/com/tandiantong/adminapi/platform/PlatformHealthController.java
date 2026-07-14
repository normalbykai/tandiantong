package com.tandiantong.adminapi.platform;

import com.tandiantong.adminapi.platform.dto.PlatformHealthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.Instant;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 平台服务健康检查接口。 */
@RestController
@RequestMapping("/api/platform/v1")
@Tag(name = "平台健康检查")
public class PlatformHealthController {

    @Operation(summary = "查询平台服务状态")
    @GetMapping("/health")
    public PlatformHealthResponse health() {
        return new PlatformHealthResponse("平台服务正常", Instant.now().toString());
    }
}
