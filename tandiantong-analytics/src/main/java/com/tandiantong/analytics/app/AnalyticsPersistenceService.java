package com.tandiantong.analytics.app;

import com.tandiantong.analytics.entity.AnalyticsExportTaskEntity;
import com.tandiantong.analytics.mapper.AnalyticsExportTaskMapper;
import com.tandiantong.analytics.mapper.AnalyticsOrderFactMapper;
import com.tandiantong.analytics.mapper.AnalyticsReservationFactMapper;
import com.tandiantong.analytics.tenant.TenantStoreScope;
import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 经营数据应用服务，负责租户经营指标查询、Excel 生成和导出审计记录。
 */
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "tandiantong.security", name = "database-enabled", havingValue = "true", matchIfMissing = true)
public class AnalyticsPersistenceService {

    private static final ZoneId BUSINESS_ZONE_ID = ZoneId.of("Asia/Shanghai");
    private static final String EXPORT_AUDIT_MESSAGE = "租户经营数据导出完成";

    private final AnalyticsOrderFactMapper orderFactMapper;
    private final AnalyticsReservationFactMapper reservationFactMapper;
    private final AnalyticsExportTaskMapper exportTaskMapper;

    /**
     * 查询指定日期范围内的订单、预约和商品排行指标。
     */
    public Dashboard dashboard(TenantStoreScope scope, LocalDate start, LocalDate end) {
        validateDateRange(start, end);
        Map<String, Object> order = orderFactMapper.selectOrderSummary(
                scope.tenantId(), scope.storeId(), start, end);
        Map<String, Object> reservation = reservationFactMapper.selectReservationSummary(
                scope.tenantId(), scope.storeId(), start, end);
        List<ProductMetric> products = orderFactMapper.selectProductRanking(
                        scope.tenantId(), scope.storeId(), start, end)
                .stream()
                .map(row -> new ProductMetric(string(row, "product_name"),
                        number(row, "quantity"), number(row, "amount_cent")))
                .toList();
        return new Dashboard(
                new Summary(number(order, "order_count"), number(order, "gross_cent"),
                        number(order, "refund_cent"), number(order, "pending_verification")),
                new ReservationMetrics(number(reservation, "total"), number(reservation, "canceled"),
                        number(reservation, "fulfilled")),
                products);
    }

    /**
     * 生成经营数据 Excel，并保存包含脱敏联系方式的导出审计记录。
     */
    @Transactional
    public ExportFile export(TenantStoreScope scope, LocalDate start, LocalDate end, String contact) {
        Dashboard dashboard = dashboard(scope, start, end);
        String fileName = "经营数据-" + start + "-" + end + ".xlsx";
        byte[] content = workbook(dashboard);

        AnalyticsExportTaskEntity task = new AnalyticsExportTaskEntity();
        task.setTenantId(scope.tenantId());
        task.setStoreId(scope.storeId());
        task.setExportType(ExportType.TRANSACTION.code());
        task.setStartDate(start);
        task.setEndDate(end);
        task.setFileName(fileName);
        task.setStatus(ExportStatus.FINISHED.code());
        task.setOperatorUserId(scope.operatorUserId());
        task.setOperatorContactMasked(mask(contact));
        task.setAuditMessage(EXPORT_AUDIT_MESSAGE);
        task.setFinishedAt(LocalDateTime.now(BUSINESS_ZONE_ID));
        exportTaskMapper.insert(task);
        return new ExportFile(fileName, content);
    }

    private byte[] workbook(Dashboard data) {
        try (var workbook = new XSSFWorkbook(); var output = new ByteArrayOutputStream()) {
            var summary = workbook.createSheet("经营概览");
            String[][] rows = {
                    {"指标", "数值"},
                    {"订单数", String.valueOf(data.order().orderCount())},
                    {"实收金额（分）", String.valueOf(data.order().grossCent())},
                    {"退款金额（分）", String.valueOf(data.order().refundCent())},
                    {"待核销订单", String.valueOf(data.order().pendingVerification())},
                    {"预约数", String.valueOf(data.reservation().total())}
            };
            for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
                var row = summary.createRow(rowIndex);
                for (int columnIndex = 0; columnIndex < rows[rowIndex].length; columnIndex++) {
                    row.createCell(columnIndex).setCellValue(rows[rowIndex][columnIndex]);
                }
            }
            var ranking = workbook.createSheet("商品排行");
            var head = ranking.createRow(0);
            head.createCell(0).setCellValue("商品");
            head.createCell(1).setCellValue("销量");
            head.createCell(2).setCellValue("销售额（分）");
            for (int index = 0; index < data.products().size(); index++) {
                ProductMetric item = data.products().get(index);
                var row = ranking.createRow(index + 1);
                row.createCell(0).setCellValue(item.name());
                row.createCell(1).setCellValue(item.quantity());
                row.createCell(2).setCellValue(item.amountCent());
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("生成经营数据 Excel 失败", exception);
        }
    }

    private int number(Map<String, Object> row, String key) {
        Object value = value(row, key);
        return value == null ? 0 : ((Number) value).intValue();
    }

    private String string(Map<String, Object> row, String key) {
        Object value = value(row, key);
        return value == null ? null : value.toString();
    }

    private Object value(Map<String, Object> row, String key) {
        if (row == null) {
            return null;
        }
        Object value = row.get(key);
        return value == null ? row.get(key.toUpperCase()) : value;
    }

    private void validateDateRange(LocalDate start, LocalDate end) {
        if (start == null || end == null || start.isAfter(end)) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "经营数据日期范围不合法");
        }
    }

    private String mask(String contact) {
        if (contact == null || contact.length() < 7) {
            return "****";
        }
        return contact.substring(0, 3) + "****" + contact.substring(contact.length() - 4);
    }

    /** 订单经营指标。 */
    public record Summary(int orderCount, int grossCent, int refundCent, int pendingVerification) {
    }

    /** 预约经营指标。 */
    public record ReservationMetrics(int total, int canceled, int fulfilled) {
    }

    /** 商品销量排行指标。 */
    public record ProductMetric(String name, int quantity, int amountCent) {
    }

    /** 经营数据看板结果。 */
    public record Dashboard(Summary order, ReservationMetrics reservation, List<ProductMetric> products) {
    }

    /** 经营数据导出文件。 */
    public record ExportFile(String fileName, byte[] content) {
    }

    /** 导出业务类型。 */
    private enum ExportType {
        /** 交易概览导出任务。 */
        TRANSACTION;

        String code() {
            return name();
        }
    }

    /** 导出任务状态。 */
    private enum ExportStatus {
        /** 导出任务已完成并生成文件。 */
        FINISHED;

        String code() {
            return name();
        }
    }
}
