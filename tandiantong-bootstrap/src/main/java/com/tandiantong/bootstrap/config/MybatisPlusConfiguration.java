package com.tandiantong.bootstrap.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.TenantLineInnerInterceptor;
import com.tandiantong.security.context.SecurityContextHolder;
import java.util.Set;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 统一配置。
 */
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
public class MybatisPlusConfiguration {

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

    /**
     * 注册分页和租户拦截器，确保后续 Mapper 查询默认具备租户隔离能力。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new TenantLineInnerInterceptor(new TenantLineHandler()));
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    private static class TenantLineHandler implements com.baomidou.mybatisplus.extension.plugins.handler.TenantLineHandler {

        /**
         * 租户编号必须来自服务端可信用户上下文，禁止从请求参数中读取。
         */
        @Override
        public Expression getTenantId() {
            return new LongValue(SecurityContextHolder.currentTenantId());
        }

        /**
         * 只有已经确认包含 tenant_id 的业务表才进入自动租户隔离。
         */
        @Override
        public boolean ignoreTable(String tableName) {
            return !TENANT_TABLES.contains(tableName);
        }
    }
}
