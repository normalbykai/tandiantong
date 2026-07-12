package com.tandiantong.analytics.domain;

import com.tandiantong.analytics.app.ExportType;

import java.time.Instant;
import java.time.LocalDate;

public record ExportTask(
        Long taskId,
        Long tenantId,
        Long storeId,
        ExportType exportType,
        LocalDate startDate,
        LocalDate endDate,
        String fileName,
        ExportStatus status,
        String operatorContact,
        String auditMessage,
        Instant createdAt
) {
}
