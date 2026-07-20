package com.tandiantong.bootstrap.config;

import com.tandiantong.framework.mybatis.core.tenant.TenantIdProvider;
import com.tandiantong.framework.mybatis.core.tenant.TenantTableProvider;
import com.tandiantong.framework.tenant.core.context.TenantContextHolder;
import java.util.Set;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** 将项目安全上下文适配给框架层 MyBatis 租户拦截器。 */
@Configuration
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
@MapperScan({
        "com.tandiantong.security.mapper",
        "com.tandiantong.catalog.mapper",
        "com.tandiantong.order.mapper",
        "com.tandiantong.reservation.mapper",
        "com.tandiantong.verification.mapper",
        "com.tandiantong.analytics.mapper"
})
public class TenantMybatisConfiguration {

    private static final Set<String> TENANT_TABLES = Set.of(
            "store",
            "admin_user",
            "role",
            "user_role",
            "role_permission",
            "operation_log",
            "merchant_invitation",
            "tenant_payment_config",
            "mini_program_scene",
            "product_category",
            "product",
            "product_sku",
            "addon_group",
            "addon_option",
            "product_addon_relation",
            "inventory_record",
            "sales_order",
            "sales_order_item",
            "payment_record",
            "refund_record",
            "order_status_log",
            "business_idempotency_record",
            "service_item",
            "service_slot",
            "service_reservation",
            "reservation_status_log",
            "pickup_no_sequence",
            "verification_credential",
            "verification_record",
            "analytics_order_fact",
            "analytics_reservation_fact",
            "analytics_export_task"
    );

    @Bean
    public TenantIdProvider tenantIdProvider() {
        return TenantContextHolder::currentTenantId;
    }

    @Bean
    public TenantTableProvider tenantTableProvider() {
        return () -> TENANT_TABLES;
    }
}
