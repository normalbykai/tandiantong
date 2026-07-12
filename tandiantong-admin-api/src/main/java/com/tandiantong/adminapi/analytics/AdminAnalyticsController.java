package com.tandiantong.adminapi.analytics;

import com.tandiantong.analytics.app.AnalyticsPersistenceService;
import com.tandiantong.analytics.tenant.TenantStoreScope;
import com.tandiantong.security.context.SecurityContextHolder;
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

@RestController
@RequestMapping("/api/admin/v1/analytics")
public class AdminAnalyticsController {
    private final AnalyticsPersistenceService service;
    public AdminAnalyticsController(AnalyticsPersistenceService service){this.service=service;}
    @GetMapping public AnalyticsPersistenceService.Dashboard dashboard(@RequestParam("startDate") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate startDate,@RequestParam("endDate") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate endDate){return service.dashboard(scope(),startDate,endDate);}
    @GetMapping("/export") public ResponseEntity<byte[]> export(@RequestParam("startDate") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate startDate,@RequestParam("endDate") @DateTimeFormat(iso=DateTimeFormat.ISO.DATE) LocalDate endDate,@RequestParam("contact") String contact){var file=service.export(scope(),startDate,endDate,contact);return ResponseEntity.ok().contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename(file.fileName(),java.nio.charset.StandardCharsets.UTF_8).build().toString()).body(file.content());}
    private TenantStoreScope scope(){var user=SecurityContextHolder.currentUser();return new TenantStoreScope(user.tenantId(),user.storeId(),user.userId());}
}
