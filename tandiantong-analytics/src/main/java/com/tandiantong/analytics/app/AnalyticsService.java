package com.tandiantong.analytics.app;

import com.tandiantong.analytics.domain.AnalyticsOrderFact;
import com.tandiantong.analytics.domain.AnalyticsReservationFact;
import com.tandiantong.analytics.domain.ExportStatus;
import com.tandiantong.analytics.domain.ExportTask;
import com.tandiantong.analytics.domain.MetricRanking;
import com.tandiantong.analytics.domain.OrderFactStatus;
import com.tandiantong.analytics.domain.ProductRankingReport;
import com.tandiantong.analytics.domain.ReservationFactStatus;
import com.tandiantong.analytics.domain.ReservationSummary;
import com.tandiantong.analytics.domain.TransactionSummary;
import com.tandiantong.analytics.tenant.TenantStoreScope;
import com.tandiantong.common.api.ErrorCode;
import com.tandiantong.common.exception.BusinessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AnalyticsService {

    private static final DateTimeFormatter FILE_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    private final AtomicLong idSequence = new AtomicLong(1000);
    private final List<AnalyticsOrderFact> orderFacts = new ArrayList<>();
    private final List<AnalyticsReservationFact> reservationFacts = new ArrayList<>();
    private final List<ExportTask> exportTasks = new ArrayList<>();

    public void recordOrder(AnalyticsOrderFact fact) {
        orderFacts.add(fact);
    }

    public void recordReservation(AnalyticsReservationFact fact) {
        reservationFacts.add(fact);
    }

    public TransactionSummary transactionSummary(TenantStoreScope scope, LocalDate startDate, LocalDate endDate) {
        List<AnalyticsOrderFact> facts = orderFacts(scope, startDate, endDate);
        int grossAmountCent = facts.stream().mapToInt(AnalyticsOrderFact::grossAmountCent).sum();
        int refundAmountCent = facts.stream().mapToInt(AnalyticsOrderFact::refundAmountCent).sum();
        int paidOrderCount = (int) facts.stream()
                .filter(fact -> fact.status() == OrderFactStatus.PAID || fact.status() == OrderFactStatus.PENDING_VERIFY)
                .count();
        int pendingVerificationCount = (int) facts.stream()
                .filter(fact -> fact.status() == OrderFactStatus.PENDING_VERIFY)
                .count();
        return new TransactionSummary(facts.size(), paidOrderCount, grossAmountCent, refundAmountCent,
                grossAmountCent - refundAmountCent, pendingVerificationCount);
    }

    public ProductRankingReport productRanking(TenantStoreScope scope, LocalDate startDate, LocalDate endDate) {
        List<AnalyticsOrderFact> facts = orderFacts(scope, startDate, endDate);
        return new ProductRankingReport(
                ranking(facts, AnalyticsOrderFact::productName),
                ranking(facts, AnalyticsOrderFact::skuName),
                addonRanking(facts)
        );
    }

    public ReservationSummary reservationSummary(TenantStoreScope scope, LocalDate startDate, LocalDate endDate) {
        List<AnalyticsReservationFact> facts = reservationFacts(scope, startDate, endDate);
        int canceledCount = (int) facts.stream().filter(fact -> fact.status() == ReservationFactStatus.CANCELED).count();
        int fulfilledCount = (int) facts.stream().filter(fact -> fact.status() == ReservationFactStatus.FULFILLED).count();
        BigDecimal averageUsageRate = facts.isEmpty() ? BigDecimal.ZERO : facts.stream()
                .map(fact -> BigDecimal.valueOf(fact.slotUsedCapacity())
                        .divide(BigDecimal.valueOf(fact.slotCapacity()), 4, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(facts.size()), 4, RoundingMode.HALF_UP);
        return new ReservationSummary(facts.size(), canceledCount, fulfilledCount, averageUsageRate);
    }

    public ExportTask createExportTask(TenantStoreScope scope, ExportType exportType, LocalDate startDate,
                                       LocalDate endDate, String operatorContact, boolean hasExportPermission) {
        if (!hasExportPermission) {
            throw businessError("当前账号没有经营数据导出权限");
        }
        if (!hasExportableData(scope, exportType, startDate, endDate)) {
            throw businessError("当前条件没有可导出的经营数据");
        }
        String fileName = "经营数据-" + exportType.displayName() + "-"
                + FILE_DATE.format(startDate) + "-" + FILE_DATE.format(endDate) + ".xlsx";
        String maskedContact = maskContact(operatorContact);
        ExportTask task = new ExportTask(idSequence.incrementAndGet(), scope.tenantId(), scope.storeId(), exportType,
                startDate, endDate, fileName, ExportStatus.FINISHED, maskedContact,
                "租户 " + scope.tenantId() + " 导出 " + exportType.displayName() + "，联系人已脱敏",
                Instant.now());
        exportTasks.add(task);
        return task;
    }

    public List<ExportTask> exportTasks(TenantStoreScope scope) {
        return exportTasks.stream()
                .filter(task -> task.tenantId().equals(scope.tenantId()) && task.storeId().equals(scope.storeId()))
                .toList();
    }

    private List<AnalyticsOrderFact> orderFacts(TenantStoreScope scope, LocalDate startDate, LocalDate endDate) {
        return orderFacts.stream()
                .filter(fact -> fact.tenantId().equals(scope.tenantId()) && fact.storeId().equals(scope.storeId()))
                .filter(fact -> !fact.businessDate().isBefore(startDate) && !fact.businessDate().isAfter(endDate))
                .toList();
    }

    private List<AnalyticsReservationFact> reservationFacts(TenantStoreScope scope, LocalDate startDate,
                                                            LocalDate endDate) {
        return reservationFacts.stream()
                .filter(fact -> fact.tenantId().equals(scope.tenantId()) && fact.storeId().equals(scope.storeId()))
                .filter(fact -> !fact.businessDate().isBefore(startDate) && !fact.businessDate().isAfter(endDate))
                .toList();
    }

    private List<MetricRanking> ranking(List<AnalyticsOrderFact> facts, Function<AnalyticsOrderFact, String> nameGetter) {
        Map<String, int[]> metrics = new LinkedHashMap<>();
        for (AnalyticsOrderFact fact : facts) {
            int[] values = metrics.computeIfAbsent(nameGetter.apply(fact), ignored -> new int[2]);
            values[0] += fact.quantity();
            values[1] += fact.grossAmountCent();
        }
        return toRanking(metrics);
    }

    private List<MetricRanking> addonRanking(List<AnalyticsOrderFact> facts) {
        Map<String, int[]> metrics = new LinkedHashMap<>();
        for (AnalyticsOrderFact fact : facts) {
            for (String addonName : fact.addonNames()) {
                int[] values = metrics.computeIfAbsent(addonName, ignored -> new int[2]);
                values[0] += 1;
                values[1] += 0;
            }
        }
        return toRanking(metrics);
    }

    private List<MetricRanking> toRanking(Map<String, int[]> metrics) {
        return metrics.entrySet().stream()
                .map(entry -> new MetricRanking(entry.getKey(), entry.getValue()[0], entry.getValue()[1]))
                .sorted(Comparator.comparing(MetricRanking::quantity).reversed()
                        .thenComparing(MetricRanking::amountCent, Comparator.reverseOrder())
                        .thenComparing(MetricRanking::name))
                .toList();
    }

    private boolean hasExportableData(TenantStoreScope scope, ExportType exportType, LocalDate startDate,
                                      LocalDate endDate) {
        return switch (exportType) {
            case TRANSACTION, PRODUCT -> !orderFacts(scope, startDate, endDate).isEmpty();
            case RESERVATION -> !reservationFacts(scope, startDate, endDate).isEmpty();
        };
    }

    private String maskContact(String contact) {
        if (contact == null || contact.isBlank()) {
            return "";
        }
        int atIndex = contact.indexOf('@');
        if (atIndex > 0) {
            return contact.charAt(0) + "***" + contact.substring(atIndex);
        }
        return contact.length() <= 4 ? "****" : contact.substring(0, 3) + "****" + contact.substring(contact.length() - 4);
    }

    private BusinessException businessError(String message) {
        return new BusinessException(ErrorCode.VALIDATION_FAILED, message);
    }
}
