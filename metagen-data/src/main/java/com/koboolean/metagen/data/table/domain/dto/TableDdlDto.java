package com.koboolean.metagen.data.table.domain.dto;

import java.util.List;

/**
 * 테이블 DDL (테이블 관리 > 테이블 상세 > DDL 보기)
 *
 * @param dbType      대상 DB (프로젝트 설정 : POSTGRESQL / ORACLE / MYSQL / MARIADB)
 * @param dbTypeLabel 대상 DB 표시명 (예: PostgreSQL)
 * @param tableName   테이블명
 * @param ddl         CREATE TABLE / INDEX / COMMENT 스크립트
 * @param warnings    확인이 필요한 항목 (길이 미지정, PK 없음 등)
 */
public record TableDdlDto(String dbType, String dbTypeLabel, String tableName, String ddl, List<String> warnings) {
}
