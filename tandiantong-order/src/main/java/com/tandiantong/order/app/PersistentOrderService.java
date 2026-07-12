package com.tandiantong.order.app;

import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import com.tandiantong.integration.wechatpay.WechatPayClient;
import com.tandiantong.integration.wechatpay.WechatPrepayResult;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PersistentOrderService {

    private final JdbcTemplate jdbcTemplate;
    private final WechatPayClient wechatPayClient;

    public PersistentOrderService(JdbcTemplate jdbcTemplate, WechatPayClient wechatPayClient) {
        this.jdbcTemplate = jdbcTemplate;
        this.wechatPayClient = wechatPayClient;
    }

    @Transactional
    public PersistentOrderResult createOrder(String sceneKey, PersistentCreateOrderCommand command) {
        Scope scope = resolveScope(sceneKey);
        validate(command);
        List<PersistedResult> previous = jdbcTemplate.query("select business_no, result_status from business_idempotency_record where tenant_id=? and business_type='ORDER_CREATE' and idempotency_key=?",
                (resultSet, rowNumber) -> new PersistedResult(resultSet.getString("business_no"), resultSet.getString("result_status")), scope.tenantId(), command.idempotencyKey());
        if (!previous.isEmpty()) {
            return findCreatedOrder(scope.tenantId(), previous.getFirst().businessNo());
        }
        String orderNo = "SO" + scope.tenantId() + UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        List<OrderLine> lines = command.lines().stream().map(line -> lockAndPrice(scope, line, orderNo)).toList();
        int amountCent = lines.stream().mapToInt(OrderLine::subtotalCent).sum();
        WechatPrepayResult prepay = wechatPayClient.createPrepay(orderNo, amountCent, "摊点通商品订单");
        insertOrder(scope, orderNo, amountCent, command.contactMobile(), command.pickupTimeText(), prepay.prepayId());
        for (OrderLine line : lines) {
            jdbcTemplate.update("insert into sales_order_item (tenant_id, store_id, order_no, sku_id, product_name, sku_text, addon_snapshot, unit_price_cent, addon_amount_cent, quantity, subtotal_cent) values (?, ?, ?, ?, ?, ?, ?, ?, 0, ?, ?)",
                    scope.tenantId(), scope.storeId(), orderNo, line.skuId(), line.productName(), line.specificationText(), "", line.unitPriceCent(), line.quantity(), line.subtotalCent());
        }
        jdbcTemplate.update("insert into business_idempotency_record (tenant_id, idempotency_key, business_type, business_no, result_status) values (?, ?, 'ORDER_CREATE', ?, 'SUCCESS')",
                scope.tenantId(), command.idempotencyKey(), orderNo);
        return new PersistentOrderResult(orderNo, amountCent, "PENDING_PAYMENT", prepay.prepayId(), prepay.payNonce());
    }

    @Transactional
    public PersistentOrderResult confirmPayment(String orderNo, String transactionId, int amountCent, String signature) {
        OrderRow order = findOrder(orderNo);
        if (!wechatPayClient.verifyCallback(orderNo, transactionId, amountCent, signature)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "微信支付回调验签失败");
        }
        if (order.status().equals("PENDING_VERIFY")) {
            return new PersistentOrderResult(orderNo, order.amountCent(), order.status(), order.prepayId(), "");
        }
        if (!order.status().equals("PENDING_PAYMENT") || order.amountCent() != amountCent) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "订单支付状态或金额不正确");
        }
        int updated = jdbcTemplate.update("update sales_order set status='PENDING_VERIFY', paid_at=current_timestamp(3) where tenant_id=? and order_no=? and status='PENDING_PAYMENT'",
                order.tenantId(), orderNo);
        if (updated == 0) {
            return findCreatedOrder(order.tenantId(), orderNo);
        }
        jdbcTemplate.update("insert into payment_record (tenant_id, store_id, order_no, transaction_id, amount_cent, status, paid_at) values (?, ?, ?, ?, ?, 'SUCCESS', current_timestamp(3))",
                order.tenantId(), order.storeId(), orderNo, transactionId, amountCent);
        for (OrderLine line : orderLines(order)) {
            jdbcTemplate.update("update product_sku set locked_stock=locked_stock-? where tenant_id=? and store_id=? and id=? and locked_stock>=?",
                    line.quantity(), order.tenantId(), order.storeId(), line.skuId(), line.quantity());
            jdbcTemplate.update("insert into inventory_record (tenant_id, store_id, sku_id, change_type, quantity, available_after, locked_after, business_no, reason, operator_user_id) select tenant_id, store_id, id, 'PAYMENT_DEDUCT', ?, available_stock, locked_stock, ?, '支付确认扣减', 0 from product_sku where id=? and tenant_id=? and store_id=?",
                    line.quantity(), orderNo, line.skuId(), order.tenantId(), order.storeId());
        }
        return findCreatedOrder(order.tenantId(), orderNo);
    }

    private OrderLine lockAndPrice(Scope scope, PersistentOrderLineCommand line, String orderNo) {
        List<OrderLine> candidates = jdbcTemplate.query("select s.id,s.specification_text,s.price_cent,p.name from product_sku s join product p on p.id=s.product_id and p.tenant_id=s.tenant_id and p.store_id=s.store_id where s.id=? and s.tenant_id=? and s.store_id=? and s.enabled=true and p.status='ON_SHELF'",
                (resultSet, rowNumber) -> new OrderLine(resultSet.getLong("id"), resultSet.getString("name"), resultSet.getString("specification_text"), resultSet.getInt("price_cent"), line.quantity(), resultSet.getInt("price_cent") * line.quantity()),
                line.skuId(), scope.tenantId(), scope.storeId());
        if (candidates.size() != 1) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商品SKU不存在或已下架");
        }
        OrderLine priced = candidates.getFirst();
        int updated = jdbcTemplate.update("update product_sku set available_stock=available_stock-?, locked_stock=locked_stock+? where id=? and tenant_id=? and store_id=? and available_stock>=?",
                line.quantity(), line.quantity(), line.skuId(), scope.tenantId(), scope.storeId(), line.quantity());
        if (updated != 1) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "商品库存不足");
        }
        jdbcTemplate.update("insert into inventory_record (tenant_id, store_id, sku_id, change_type, quantity, available_after, locked_after, business_no, reason, operator_user_id) select tenant_id, store_id, id, 'ORDER_LOCK', ?, available_stock, locked_stock, ?, '订单锁定库存', 0 from product_sku where id=? and tenant_id=? and store_id=?",
                line.quantity(), orderNo, line.skuId(), scope.tenantId(), scope.storeId());
        return priced;
    }

    private void insertOrder(Scope scope, String orderNo, int amountCent, String mobile, String pickupTime, String prepayId) {
        jdbcTemplate.update("insert into sales_order (tenant_id, store_id, order_no, status, pay_amount_cent, contact_mobile, pickup_time_text, prepay_id) values (?, ?, ?, 'PENDING_PAYMENT', ?, ?, ?, ?)",
                scope.tenantId(), scope.storeId(), orderNo, amountCent, mobile, pickupTime, prepayId);
    }

    private Scope resolveScope(String sceneKey) {
        List<Scope> scopes = jdbcTemplate.query("select tenant_id,store_id from mini_program_scene where scene_key=? and enabled=true",
                (resultSet, rowNumber) -> new Scope(resultSet.getLong("tenant_id"), resultSet.getLong("store_id")), sceneKey);
        if (scopes.size() != 1) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "商户入口码无效");
        }
        return scopes.getFirst();
    }

    private PersistentOrderResult findCreatedOrder(Long tenantId, String orderNo) {
        List<OrderRow> rows = jdbcTemplate.query("select tenant_id,store_id,order_no,status,pay_amount_cent,prepay_id from sales_order where tenant_id=? and order_no=?",
                (resultSet, rowNumber) -> new OrderRow(resultSet.getLong("tenant_id"), resultSet.getLong("store_id"), resultSet.getString("order_no"), resultSet.getString("status"), resultSet.getInt("pay_amount_cent"), resultSet.getString("prepay_id")), tenantId, orderNo);
        if (rows.size() != 1) {
            throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "订单不存在");
        }
        OrderRow row = rows.getFirst();
        return new PersistentOrderResult(row.orderNo(), row.amountCent(), row.status(), row.prepayId(), "");
    }

    private OrderRow findOrder(String orderNo) {
        List<OrderRow> rows = jdbcTemplate.query("select tenant_id,store_id,order_no,status,pay_amount_cent,prepay_id from sales_order where order_no=?",
                (resultSet, rowNumber) -> new OrderRow(resultSet.getLong("tenant_id"), resultSet.getLong("store_id"), resultSet.getString("order_no"), resultSet.getString("status"), resultSet.getInt("pay_amount_cent"), resultSet.getString("prepay_id")), orderNo);
        if (rows.size() != 1) throw new BusinessException(ErrorCode.RESOURCE_NOT_FOUND, "订单不存在");
        return rows.getFirst();
    }

    private List<OrderLine> orderLines(OrderRow order) {
        return jdbcTemplate.query("select sku_id,product_name,sku_text,unit_price_cent,quantity,subtotal_cent from sales_order_item where tenant_id=? and store_id=? and order_no=?",
                (resultSet, rowNumber) -> new OrderLine(resultSet.getLong("sku_id"), resultSet.getString("product_name"), resultSet.getString("sku_text"), resultSet.getInt("unit_price_cent"), resultSet.getInt("quantity"), resultSet.getInt("subtotal_cent")), order.tenantId(), order.storeId(), order.orderNo());
    }

    private void validate(PersistentCreateOrderCommand command) {
        if (command.idempotencyKey()==null || command.idempotencyKey().isBlank() || command.contactMobile()==null || !command.contactMobile().matches("1\\d{10}") || command.lines()==null || command.lines().isEmpty()) throw new BusinessException(ErrorCode.VALIDATION_FAILED,"下单参数不合法");
        if (command.lines().stream().anyMatch(line -> line.skuId()==null || line.quantity()<=0)) throw new BusinessException(ErrorCode.VALIDATION_FAILED,"商品数量不合法");
    }

    public record PersistentCreateOrderCommand(String idempotencyKey,String contactMobile,String pickupTimeText,List<PersistentOrderLineCommand> lines) {}
    public record PersistentOrderLineCommand(Long skuId,int quantity) {}
    public record PersistentOrderResult(String orderNo,int payAmountCent,String status,String prepayId,String paymentParameters) {}
    private record Scope(Long tenantId,Long storeId) {}
    private record PersistedResult(String businessNo,String status) {}
    private record OrderRow(Long tenantId,Long storeId,String orderNo,String status,int amountCent,String prepayId) {}
    private record OrderLine(Long skuId,String productName,String specificationText,int unitPriceCent,int quantity,int subtotalCent) {}
}
