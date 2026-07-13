package com.tandiantong.analytics.app;

/** 经营数据导出类型。 */
public enum ExportType {
    TRANSACTION("交易概览"),
    PRODUCT("商品分析"),
    RESERVATION("预约分析");

    private final String displayName;

    ExportType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
