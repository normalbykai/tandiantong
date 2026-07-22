package com.tandiantong.security.audit;

/** 审计对象的稳定标识和供人员阅读的名称快照。 */
public class AuditTarget {
    private final String type;
    private final String id;
    private final String name;
    private final String code;

    private AuditTarget(String type, String id, String name, String code) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.code = code;
    }

    public static AuditTarget of(String type, Long id, String name) {
        return new AuditTarget(type, String.valueOf(id), name, null);
    }

    public static AuditTarget of(String type, Long id, String name, String code) {
        return new AuditTarget(type, String.valueOf(id), name, code);
    }

    public static AuditTarget of(String type, String id) {
        return new AuditTarget(type, id, id, null);
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String displayName() {
        if (code == null || code.isBlank()) {
            return name;
        }
        return name + "（" + code + "）";
    }
}
