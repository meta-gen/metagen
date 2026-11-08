package com.koboolean.metagen.core.project.domain.enums;

import java.util.Arrays;

/**
 * 프로젝트 대상 DB (시스템 관리 > 프로젝트 관리에서 설정) : 테이블 DDL 생성 기준
 */
public enum DbType {
    POSTGRESQL("PostgreSQL"),
    ORACLE("Oracle"),
    MYSQL("MySQL"),
    MARIADB("MariaDB");

    public static final DbType DEFAULT = POSTGRESQL;

    private final String label;

    DbType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * 문자열(enum 이름, 대소문자 무관)을 DbType 으로 바꾼다. 비었거나 모르는 값이면 기본값(PostgreSQL)
     */
    public static DbType fromOrDefault(String value) {
        if (value == null || value.isBlank()) return DEFAULT;
        return Arrays.stream(values())
                .filter(type -> type.name().equalsIgnoreCase(value.trim()))
                .findFirst()
                .orElse(DEFAULT);
    }

    public static DbType orDefault(DbType dbType) {
        return dbType == null ? DEFAULT : dbType;
    }
}
