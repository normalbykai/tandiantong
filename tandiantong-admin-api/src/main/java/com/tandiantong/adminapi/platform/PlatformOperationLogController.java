package com.tandiantong.adminapi.platform;

import cn.dev33.satoken.annotation.SaCheckPermission;

import com.tandiantong.security.platform.PlatformOperationLogQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 平台操作日志查询接口。 */
@RestController
@ConditionalOnProperty(
        prefix = "tandiantong.security",
        name = "database-enabled",
        havingValue = "true",
        matchIfMissing = true)
@RequestMapping("/api/platform/v1/logs")
@Tag(name = "平台操作日志", description = "查询平台管理端的关键操作审计记录，只读不允许删除")
public class PlatformOperationLogController {
    private final PlatformOperationLogQueryService service;

    public PlatformOperationLogController(PlatformOperationLogQueryService service) {
        this.service = service;
    }

    @GetMapping
    @SaCheckPermission("platform:operation-log:read")
    @Operation(summary = "查询平台操作日志", description = "按关键字、对象类型、操作类型和时间范围查询平台操作审计记录")
    public PlatformOperationLogPageResponse list(
            @Parameter(description = "关键字，匹配操作人、操作类型、对象、追踪号和详情", example = "平台")
            @RequestParam(value = "keyword", required = false) String keyword,
            @Parameter(description = "操作类型筛选", example = "停用平台账号")
            @RequestParam(value = "operationType", required = false) String operationType,
            @Parameter(description = "操作对象类型筛选", example = "平台账号")
            @RequestParam(value = "targetType", required = false) String targetType,
            @Parameter(description = "追踪号筛选", example = "trace-001")
            @RequestParam(value = "traceId", required = false) String traceId,
            @Parameter(description = "开始日期，按创建时间过滤", example = "2026-07-21")
            @RequestParam(value = "startDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @Parameter(description = "结束日期，按创建时间过滤", example = "2026-07-21")
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            @Parameter(description = "页码，从 1 开始", example = "1")
            @RequestParam(value = "page", defaultValue = "1")
            long page,
            @Parameter(description = "每页条数，最大 100", example = "20")
            @RequestParam(value = "pageSize", defaultValue = "20")
            long pageSize) {
        PlatformOperationLogQueryService.PlatformOperationLogPage result =
                service.listPlatformLogs(keyword, operationType, targetType, traceId, startDate, endDate, page, pageSize);
        return PlatformOperationLogPageResponse.from(result);
    }

    @Schema(description = "平台操作日志分页响应")
    public static class PlatformOperationLogPageResponse {
        private long total;
        private long current;
        private long pageSize;
        private List<PlatformOperationLogItemResponse> records;

        static PlatformOperationLogPageResponse from(
                PlatformOperationLogQueryService.PlatformOperationLogPage source) {
            PlatformOperationLogPageResponse response = new PlatformOperationLogPageResponse();
            response.total = source.total();
            response.current = source.current();
            response.pageSize = source.pageSize();
            response.records = source.records().stream().map(PlatformOperationLogItemResponse::from).toList();
            return response;
        }

        public long getTotal() {
            return total;
        }

        public long getCurrent() {
            return current;
        }

        public long getPageSize() {
            return pageSize;
        }

        public List<PlatformOperationLogItemResponse> getRecords() {
            return records;
        }
    }

    @Schema(description = "平台操作日志响应项")
    public static class PlatformOperationLogItemResponse {
        private Long id;
        private Long operatorId;
        private String operatorName;
        private String operatorMobile;
        private String operationType;
        private String targetType;
        private String targetId;
        private String detail;
        private String traceId;
        private String userIp;
        private String requestMethod;
        private String requestUrl;
        private LocalDateTime createdAt;

        static PlatformOperationLogItemResponse from(
                PlatformOperationLogQueryService.PlatformOperationLogItem source) {
            PlatformOperationLogItemResponse response = new PlatformOperationLogItemResponse();
            response.id = source.getId();
            response.operatorId = source.getOperatorId();
            response.operatorName = source.getOperatorName();
            response.operatorMobile = source.getOperatorMobile();
            response.operationType = source.getOperationType();
            response.targetType = source.getTargetType();
            response.targetId = source.getTargetId();
            response.detail = source.getDetail();
            response.traceId = source.getTraceId();
            response.userIp = source.getUserIp();
            response.requestMethod = source.getRequestMethod();
            response.requestUrl = source.getRequestUrl();
            response.createdAt = source.getCreatedAt();
            return response;
        }

        public Long getId() {
            return id;
        }

        public Long getOperatorId() {
            return operatorId;
        }

        public String getOperatorName() {
            return operatorName;
        }

        public String getOperatorMobile() {
            return operatorMobile;
        }

        public String getOperationType() {
            return operationType;
        }

        public String getTargetType() {
            return targetType;
        }

        public String getTargetId() {
            return targetId;
        }

        public String getDetail() {
            return detail;
        }

        public String getTraceId() {
            return traceId;
        }

        public String getUserIp() {
            return userIp;
        }

        public String getRequestMethod() {
            return requestMethod;
        }

        public String getRequestUrl() {
            return requestUrl;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }
    }
}
