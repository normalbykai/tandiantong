package com.tandiantong.adminapi.analytics;

import com.tandiantong.adminapi.analytics.dto.AnalyticsDashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.tandiantong.analytics.app.AnalyticsPersistenceService;
import com.tandiantong.analytics.tenant.TenantStoreScope;
import com.tandiantong.security.context.SecurityContextHolder;
import cn.dev33.satoken.annotation.SaCheckPermission;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 商户后台经营数据看板与导出接口。 */
@RestController
@RequestMapping("/api/admin/v1/analytics")
@Tag(name = "经营数据", description = "商户后台查询和导出订单、预约及商品经营指标")
public class AdminAnalyticsController {
    private final AnalyticsPersistenceService service;
    public AdminAnalyticsController(AnalyticsPersistenceService service){this.service=service;}
    @Operation(summary = "查询经营数据看板", description = "按日期范围统计当前租户和门店的订单、退款、预约及商品销量")
    @GetMapping @SaCheckPermission("analytics:dashboard:read") public AnalyticsDashboardResponse dashboard(
            @Parameter(description = "统计开始日期", example = "2026-07-01", required = true)
            @RequestParam("startDate") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "统计结束日期", example = "2026-07-15", required = true)
            @RequestParam("endDate") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate endDate){return AnalyticsDashboardResponse.from(service.dashboard(scope(),startDate,endDate));}
    @Operation(summary = "导出经营数据 Excel", description = "按日期范围导出经营数据，并记录导出联系人用于审计")
    @GetMapping("/export") @SaCheckPermission("analytics:export:create") public ResponseEntity<byte[]> export(
            @Parameter(description = "统计开始日期", example = "2026-07-01", required = true)
            @RequestParam("startDate") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "统计结束日期", example = "2026-07-15", required = true)
            @RequestParam("endDate") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "导出联系人，写入导出审计记录", example = "张店长", required = true)
            @RequestParam("contact") String contact){var file=service.export(scope(),startDate,endDate,contact);return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(file.fileName(),java.nio.charset.StandardCharsets.UTF_8).build().toString()).body(file.content());}
    private TenantStoreScope scope(){var user=SecurityContextHolder.currentUser();return new TenantStoreScope(user.tenantId(),user.storeId(),user.userId());}
}
