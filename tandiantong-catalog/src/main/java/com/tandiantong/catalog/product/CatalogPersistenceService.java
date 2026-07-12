package com.tandiantong.catalog.product;

import com.tandiantong.catalog.tenant.TenantStoreScope;
import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogPersistenceService {

    private final JdbcTemplate jdbcTemplate;

    public CatalogPersistenceService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public PersistedProduct createProduct(TenantStoreScope scope, CreateCatalogProductCommand command) {
        validate(command);
        Long categoryId = findOrCreateCategory(scope, command.categoryName());
        Long productId = insertId("insert into product (tenant_id, store_id, category_id, name, base_price_cent, status) values (?, ?, ?, ?, ?, ?)",
                scope.tenantId(), scope.storeId(), categoryId, command.productName(), command.basePriceCent(), command.onShelf() ? "ON_SHELF" : "DRAFT");
        List<PersistedSku> skus = command.skus().stream().map(sku -> createSku(scope, productId, sku)).toList();
        return new PersistedProduct(productId, command.productName(), command.categoryName(), command.basePriceCent(), command.onShelf(), skus);
    }

    public List<MiniProduct> listOnShelfByScene(String sceneKey) {
        return jdbcTemplate.query("select p.id, p.name, p.description, p.base_price_cent, coalesce(c.name, '推荐') as category_name, s.id sku_id, s.price_cent sku_price, s.available_stock "
                        + "from mini_program_scene m join product p on p.tenant_id=m.tenant_id and p.store_id=m.store_id "
                        + "left join product_category c on c.id=p.category_id and c.tenant_id=p.tenant_id and c.store_id=p.store_id "
                        + "join product_sku s on s.product_id=p.id and s.tenant_id=p.tenant_id and s.store_id=p.store_id and s.enabled=true "
                        + "where m.scene_key=? and m.enabled=true and p.status='ON_SHELF' and s.id=(select min(s2.id) from product_sku s2 where s2.tenant_id=p.tenant_id and s2.store_id=p.store_id and s2.product_id=p.id and s2.enabled=true) order by p.sort_order, p.id",
                (resultSet, rowNumber) -> new MiniProduct(resultSet.getLong("id"), resultSet.getString("name"),
                        resultSet.getString("description"), resultSet.getInt("sku_price"), resultSet.getString("category_name"),resultSet.getLong("sku_id"),resultSet.getInt("available_stock")), sceneKey);
    }

    public List<AdminProduct> listProducts(TenantStoreScope scope) {
        return jdbcTemplate.query("select p.id,p.name,c.name category_name,p.status,p.base_price_cent from product p left join product_category c on c.id=p.category_id and c.tenant_id=p.tenant_id and c.store_id=p.store_id where p.tenant_id=? and p.store_id=? order by p.id desc limit 200",
                (rs, row) -> new AdminProduct(rs.getLong("id"), rs.getString("name"), rs.getString("category_name"), rs.getString("status"), rs.getInt("base_price_cent"), listSkus(scope, rs.getLong("id"))), scope.tenantId(), scope.storeId());
    }

    public List<InventoryRecordView> listInventoryRecords(TenantStoreScope scope) {
        return jdbcTemplate.query("select r.id,r.created_at,r.change_type,p.name product_name,s.specification_text,r.quantity,r.business_no,r.reason from inventory_record r join product_sku s on s.id=r.sku_id and s.tenant_id=r.tenant_id and s.store_id=r.store_id join product p on p.id=s.product_id and p.tenant_id=s.tenant_id and p.store_id=s.store_id where r.tenant_id=? and r.store_id=? order by r.id desc limit 500",
                (rs, row) -> new InventoryRecordView(rs.getLong("id"), rs.getTimestamp("created_at").toLocalDateTime(), rs.getString("change_type"), rs.getString("product_name"), rs.getString("specification_text"), rs.getInt("quantity"), rs.getString("business_no"), rs.getString("reason")), scope.tenantId(), scope.storeId());
    }

    private List<AdminSku> listSkus(TenantStoreScope scope, Long productId) {
        return jdbcTemplate.query("select id,specification_text,sku_code,price_cent,available_stock,locked_stock,warning_stock from product_sku where tenant_id=? and store_id=? and product_id=? order by id",
                (rs, row) -> new AdminSku(rs.getLong("id"), rs.getString("specification_text"), rs.getString("sku_code"), rs.getInt("price_cent"), rs.getInt("available_stock"), rs.getInt("locked_stock"), rs.getInt("warning_stock")), scope.tenantId(), scope.storeId(), productId);
    }

    private PersistedSku createSku(TenantStoreScope scope, Long productId, CreateCatalogSkuCommand sku) {
        Long skuId = insertId("insert into product_sku (tenant_id, store_id, product_id, specification_text, sku_code, price_cent, available_stock, locked_stock, warning_stock, enabled) values (?, ?, ?, ?, ?, ?, ?, 0, ?, true)",
                scope.tenantId(), scope.storeId(), productId, sku.specificationText(), sku.skuCode(), sku.priceCent(), sku.initialStock(), sku.warningStock());
        jdbcTemplate.update("insert into inventory_record (tenant_id, store_id, sku_id, change_type, quantity, available_after, locked_after, business_no, reason, operator_user_id) values (?, ?, ?, ?, ?, ?, 0, ?, ?, ?)",
                scope.tenantId(), scope.storeId(), skuId, InventoryChangeType.INITIAL_STOCK.name(), sku.initialStock(), sku.initialStock(),
                "PRODUCT-" + productId, "商品初始库存", scope.operatorUserId());
        return new PersistedSku(skuId, sku.specificationText(), sku.skuCode(), sku.priceCent(), sku.initialStock(), sku.warningStock());
    }

    private Long findOrCreateCategory(TenantStoreScope scope, String categoryName) {
        List<Long> ids = jdbcTemplate.query("select id from product_category where tenant_id=? and store_id=? and name=?",
                (resultSet, rowNumber) -> resultSet.getLong(1), scope.tenantId(), scope.storeId(), categoryName);
        if (!ids.isEmpty()) {
            return ids.getFirst();
        }
        return insertId("insert into product_category (tenant_id, store_id, name) values (?, ?, ?)", scope.tenantId(), scope.storeId(), categoryName);
    }

    private Long insertId(String sql, Object... values) {
        return jdbcTemplate.execute((ConnectionCallback<Long>) connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int index = 0; index < values.length; index++) {
                statement.setObject(index + 1, values[index]);
            }
            statement.executeUpdate();
            try (var keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new IllegalStateException("数据库未返回主键");
                }
                return keys.getLong(1);
            }
        });
    }

    private void validate(CreateCatalogProductCommand command) {
        if (command.productName() == null || command.productName().isBlank() || command.categoryName() == null || command.categoryName().isBlank()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "商品名称和分类不能为空");
        }
        if (command.basePriceCent() < 0 || command.skus() == null || command.skus().isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "商品价格或SKU配置不合法");
        }
        for (CreateCatalogSkuCommand sku : command.skus()) {
            if (sku.priceCent() < 0 || sku.initialStock() < 0 || sku.warningStock() < 0 || sku.skuCode() == null || sku.skuCode().isBlank()) {
                throw new BusinessException(ErrorCode.VALIDATION_FAILED, "SKU价格、库存或编码不合法");
            }
        }
    }

    public record CreateCatalogProductCommand(String productName, String categoryName, int basePriceCent, boolean onShelf,
                                              List<CreateCatalogSkuCommand> skus) {
    }

    public record CreateCatalogSkuCommand(String specificationText, String skuCode, int priceCent, int initialStock,
                                          int warningStock) {
    }

    public record PersistedProduct(Long productId, String productName, String categoryName, int basePriceCent, boolean onShelf,
                                   List<PersistedSku> skus) {
    }

    public record PersistedSku(Long skuId, String specificationText, String skuCode, int priceCent, int availableStock,
                               int warningStock) {
    }

    public record MiniProduct(Long productId, String productName, String description, int priceCent, String categoryName,Long skuId,int availableStock) {
    }
    public record AdminProduct(Long productId,String productName,String categoryName,String status,int basePriceCent,List<AdminSku> skus) {}
    public record AdminSku(Long skuId,String specificationText,String skuCode,int priceCent,int availableStock,int lockedStock,int warningStock) {}
    public record InventoryRecordView(Long id,java.time.LocalDateTime createdAt,String changeType,String productName,String specificationText,int quantity,String businessNo,String reason) {}
}
