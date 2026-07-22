package com.tandiantong.security.platform;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tandiantong.framework.common.api.ErrorCode;
import com.tandiantong.framework.common.exception.BusinessException;
import com.tandiantong.security.context.AccessDomain;
import com.tandiantong.security.entity.OperationLogEntity;
import com.tandiantong.security.entity.PlatformUserEntity;
import com.tandiantong.security.entity.RoleEntity;
import com.tandiantong.security.entity.TenantEntity;
import com.tandiantong.security.mapper.OperationLogMapper;
import com.tandiantong.security.mapper.PlatformUserMapper;
import com.tandiantong.security.mapper.RoleMapper;
import com.tandiantong.security.mapper.TenantMapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** 平台操作日志查询服务。 */
@Service
@ConditionalOnProperty(
        prefix = "tandiantong.security",
        name = "database-enabled",
        havingValue = "true",
        matchIfMissing = true)
public class PlatformOperationLogQueryService {
    private static final String PLATFORM_DOMAIN = AccessDomain.PLATFORM.name();
    private static final int MAX_PAGE_SIZE = 100;
    private final OperationLogMapper operationLogMapper;
    private final PlatformUserMapper platformUserMapper;
    private final RoleMapper roleMapper;
    private final TenantMapper tenantMapper;

    public PlatformOperationLogQueryService(
            OperationLogMapper operationLogMapper,
            PlatformUserMapper platformUserMapper,
            RoleMapper roleMapper,
            TenantMapper tenantMapper) {
        this.operationLogMapper = operationLogMapper;
        this.platformUserMapper = platformUserMapper;
        this.roleMapper = roleMapper;
        this.tenantMapper = tenantMapper;
    }

    public PlatformOperationLogPage listPlatformLogs(
            String keyword,
            String operationType,
            String targetType,
            String traceId,
            LocalDate startDate,
            LocalDate endDate,
            long page,
            long pageSize) {
        if (page < 1) {
            throw error("页码必须大于0");
        }
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw error("每页条数必须介于1到100之间");
        }
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw error("结束日期不能早于开始日期");
        }
        LambdaQueryWrapper<OperationLogEntity> query = new LambdaQueryWrapper<OperationLogEntity>()
                .eq(OperationLogEntity::getDomain, PLATFORM_DOMAIN)
                .isNull(OperationLogEntity::getTenantId)
                .orderByDesc(OperationLogEntity::getCreatedAt)
                .orderByDesc(OperationLogEntity::getId);
        if (operationType != null && !operationType.isBlank()) {
            query.eq(OperationLogEntity::getOperationType, operationType.trim());
        }
        if (targetType != null && !targetType.isBlank()) {
            query.eq(OperationLogEntity::getTargetType, targetType.trim());
        }
        if (traceId != null && !traceId.isBlank()) {
            query.like(OperationLogEntity::getTraceId, traceId.trim());
        }
        if (startDate != null) {
            query.ge(OperationLogEntity::getCreatedAt, startDate.atStartOfDay());
        }
        if (endDate != null) {
            query.lt(OperationLogEntity::getCreatedAt, endDate.plusDays(1).atStartOfDay());
        }
        String trimmedKeyword = keyword == null ? null : keyword.trim();
        if (trimmedKeyword != null && !trimmedKeyword.isBlank()) {
            List<Long> operatorIds = matchedOperatorIds(trimmedKeyword);
            query.and(group -> {
                group.like(OperationLogEntity::getOperationType, trimmedKeyword)
                        .or()
                        .like(OperationLogEntity::getTargetType, trimmedKeyword)
                        .or()
                        .like(OperationLogEntity::getTargetId, trimmedKeyword)
                        .or()
                        .like(OperationLogEntity::getDetail, trimmedKeyword)
                        .or()
                        .like(OperationLogEntity::getTraceId, trimmedKeyword)
                        .or()
                        .like(OperationLogEntity::getUserIp, trimmedKeyword)
                        .or()
                        .like(OperationLogEntity::getRequestUrl, trimmedKeyword);
                if (!operatorIds.isEmpty()) {
                    group.or().in(OperationLogEntity::getOperatorId, operatorIds);
                }
            });
        }
        Page<OperationLogEntity> result =
                operationLogMapper.selectPage(new Page<>(page, pageSize), query);
        Map<Long, PlatformUserEntity> operatorMap = loadOperatorMap(result.getRecords());
        Map<String, String> targetNameMap = loadTargetNameMap(result.getRecords());
        List<PlatformOperationLogItem> items =
                result.getRecords().stream()
                        .map(item -> PlatformOperationLogItem.from(
                                item,
                                item.getOperatorId() == null ? null : operatorMap.get(item.getOperatorId()),
                                targetNameMap.get(targetKey(item))))
                        .toList();
        return new PlatformOperationLogPage(result.getTotal(), result.getCurrent(), result.getSize(), items);
    }

    private List<Long> matchedOperatorIds(String keyword) {
        return platformUserMapper.selectList(
                        new LambdaQueryWrapper<PlatformUserEntity>()
                                .like(PlatformUserEntity::getDisplayName, keyword)
                                .or()
                                .like(PlatformUserEntity::getMobile, keyword))
                .stream()
                .map(PlatformUserEntity::getId)
                .distinct()
                .toList();
    }

    private Map<Long, PlatformUserEntity> loadOperatorMap(List<OperationLogEntity> logs) {
        Set<Long> operatorIds =
                logs.stream()
                        .map(OperationLogEntity::getOperatorId)
                        .filter(id -> id != null)
                        .collect(Collectors.toSet());
        if (operatorIds.isEmpty()) {
            return Map.of();
        }
        return platformUserMapper.selectList(
                        new LambdaQueryWrapper<PlatformUserEntity>()
                                .in(PlatformUserEntity::getId, operatorIds))
                .stream()
                .collect(Collectors.toMap(PlatformUserEntity::getId, Function.identity()));
    }

    /** 为历史简略日志补充仍可查询到的角色或商户名称，不修改原始审计记录。 */
    private Map<String, String> loadTargetNameMap(List<OperationLogEntity> logs) {
        Set<Long> roleIds = targetIds(logs, "平台角色");
        Set<Long> tenantIds = targetIds(logs, "商户租户");
        Map<String, String> result = new java.util.HashMap<>();
        if (!roleIds.isEmpty()) {
            roleMapper.selectList(new LambdaQueryWrapper<RoleEntity>().in(RoleEntity::getId, roleIds))
                    .forEach(role -> result.put(targetKey("平台角色", role.getId().toString()), role.getName()));
        }
        if (!tenantIds.isEmpty()) {
            tenantMapper.selectList(new LambdaQueryWrapper<TenantEntity>().in(TenantEntity::getId, tenantIds))
                    .forEach(tenant -> result.put(targetKey("商户租户", tenant.getId().toString()), tenant.getName()));
        }
        return result;
    }

    private Set<Long> targetIds(List<OperationLogEntity> logs, String targetType) {
        return logs.stream()
                .filter(log -> targetType.equals(log.getTargetType()))
                .map(OperationLogEntity::getTargetId)
                .map(this::parseTargetId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
    }

    private Long parseTargetId(String targetId) {
        try {
            return targetId == null ? null : Long.valueOf(targetId);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static String targetKey(OperationLogEntity log) {
        return targetKey(log.getTargetType(), log.getTargetId());
    }

    private static String targetKey(String targetType, String targetId) {
        return targetType + ":" + targetId;
    }

    private BusinessException error(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }

    public static class PlatformOperationLogPage {
        private final long total;
        private final long current;
        private final long pageSize;
        private final List<PlatformOperationLogItem> records;

        public PlatformOperationLogPage(long total, long current, long pageSize, List<PlatformOperationLogItem> records) {
            this.total = total;
            this.current = current;
            this.pageSize = pageSize;
            this.records = records;
        }

        public long total() {
            return total;
        }

        public long current() {
            return current;
        }

        public long pageSize() {
            return pageSize;
        }

        public List<PlatformOperationLogItem> records() {
            return records;
        }
    }

    public static class PlatformOperationLogItem {
        private Long id;
        private Long operatorId;
        private String operatorName;
        private String operatorMobile;
        private String operationType;
        private String targetType;
        private String targetId;
        private String targetName;
        private boolean sensitive;
        private String detail;
        private String traceId;
        private String userIp;
        private String requestMethod;
        private String requestUrl;
        private String userAgent;
        private LocalDateTime createdAt;

        static PlatformOperationLogItem from(
                OperationLogEntity source, PlatformUserEntity operator, String targetName) {
            PlatformOperationLogItem item = new PlatformOperationLogItem();
            item.id = source.getId();
            item.operatorId = source.getOperatorId();
            item.operatorName = operator == null ? null : operator.getDisplayName();
            item.operatorMobile = operator == null ? null : operator.getMobile();
            item.operationType = source.getOperationType();
            item.targetType = source.getTargetType();
            item.targetId = source.getTargetId();
            item.targetName = targetName;
            item.sensitive = PlatformOperationLogQueryService.isSensitive(source.getOperationType());
            item.detail = resolveDetail(source, targetName);
            item.traceId = source.getTraceId();
            item.userIp = source.getUserIp();
            item.requestMethod = source.getRequestMethod();
            item.requestUrl = source.getRequestUrl();
            item.userAgent = source.getUserAgent();
            item.createdAt = source.getCreatedAt();
            return item;
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

        public String getTargetName() {
            return targetName;
        }

        public boolean isSensitive() {
            return sensitive;
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

        public String getUserAgent() {
            return userAgent;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        private static String resolveDetail(OperationLogEntity source, String targetName) {
            if (targetName == null || targetName.isBlank()) {
                return source.getDetail();
            }
            if ("平台角色".equals(source.getTargetType())
                    && "更新角色状态".equals(source.getDetail())) {
                return source.getOperationType() + "：" + targetName;
            }
            if ("商户租户".equals(source.getTargetType())
                    && "停用商户后台访问和业务写操作".equals(source.getDetail())) {
                return "已停用商户：" + targetName + "；商户后台将无法登录，且不能发起新的业务操作";
            }
            return source.getDetail();
        }
    }

    private static boolean isSensitive(String operationType) {
        if (operationType == null) {
            return false;
        }
        return operationType.contains("删除")
                || operationType.contains("停用")
                || operationType.contains("禁用")
                || operationType.contains("启用")
                || operationType.contains("重置")
                || operationType.contains("权限")
                || operationType.contains("退款")
                || operationType.contains("核销");
    }
}
