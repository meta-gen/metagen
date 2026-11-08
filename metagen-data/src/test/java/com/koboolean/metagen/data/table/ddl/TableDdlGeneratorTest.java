package com.koboolean.metagen.data.table.ddl;

import com.koboolean.metagen.core.project.domain.enums.DbType;
import com.koboolean.metagen.data.column.domain.entity.ColumnInfo;
import com.koboolean.metagen.data.table.domain.entity.TableInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class TableDdlGeneratorTest {

    private static TableInfo table() {
        return TableInfo.builder().tableName("TB_USER").tableKorName("사용자").build();
    }

    /** USER_ID(PK) / USER_NM(필수, 설명에 ' 포함) / EMAIL(고유) / AMT(숫자 12,2) / USE_YN(기본값 Y) / REG_DT(인덱스) */
    private static List<ColumnInfo> columns() {
        return List.of(
                ColumnInfo.builder().columnName("REG_DT").columnDesc("등록 일시").dataType("TIMESTAMP").isNullable(true).isIndex(true).sortOrder(6).build(),
                ColumnInfo.builder().columnName("USER_ID").columnDesc("사용자 아이디").dataType("VARCHAR").maxLength(20).isPk(true).isNullable(false).sortOrder(1).build(),
                ColumnInfo.builder().columnName("USER_NM").columnDesc("사용자's 이름").dataType("VARCHAR(100)").isRequired(true).isNullable(true).sortOrder(2).build(),
                ColumnInfo.builder().columnName("EMAIL").columnDesc("이메일").dataType("VARCHAR").maxLength(200).isUnique(true).isNullable(true).sortOrder(3).build(),
                ColumnInfo.builder().columnName("AMT").columnDesc("금액").dataType("NUMBER").precision(new BigDecimal(12)).scale(2).isNullable(true).sortOrder(4).build(),
                ColumnInfo.builder().columnName("USE_YN").columnDesc("사용 여부").dataType("CHAR").maxLength(1).defaultValue("Y").isNullable(false).sortOrder(5).build()
        );
    }

    @Test
    @DisplayName("PostgreSQL : 정렬 순서대로 컬럼, PK/UK 제약, 인덱스, COMMENT ON 을 만든다")
    void postgresql() {
        TableDdlGenerator.Result result = TableDdlGenerator.generate(DbType.POSTGRESQL, table(), columns());
        String ddl = result.ddl();

        assertThat(ddl).contains("CREATE TABLE TB_USER (")
                .contains("USER_ID VARCHAR(20) NOT NULL")
                .contains("USER_NM VARCHAR(100) NOT NULL")      // 필수 입력은 NOT NULL
                .contains("AMT     NUMERIC(12,2)")
                .contains("USE_YN  CHAR(1) DEFAULT 'Y' NOT NULL") // 문자 기본값은 따옴표
                .contains("REG_DT  TIMESTAMP")
                .contains("CONSTRAINT PK_TB_USER PRIMARY KEY (USER_ID)")
                .contains("CONSTRAINT UK_TB_USER_EMAIL UNIQUE (EMAIL)")
                .contains("CREATE INDEX IX_TB_USER_REG_DT ON TB_USER (REG_DT);")
                .contains("COMMENT ON TABLE TB_USER IS '사용자';")
                .contains("COMMENT ON COLUMN TB_USER.USER_NM IS '사용자''s 이름';") // ' 이스케이프
                .doesNotContain("ENGINE=");
        assertThat(ddl.indexOf("USER_ID")).isLessThan(ddl.indexOf("REG_DT  TIMESTAMP")); // 정렬 순서
        assertThat(result.warnings()).isEmpty();
    }

    @Test
    @DisplayName("Oracle : VARCHAR2 / NUMBER / 30자 제약명 / COMMENT ON")
    void oracle() {
        String ddl = TableDdlGenerator.generate(DbType.ORACLE, table(), columns()).ddl();

        assertThat(ddl).contains("USER_ID VARCHAR2(20) NOT NULL")
                .contains("AMT     NUMBER(12,2)")
                .contains("REG_DT  TIMESTAMP")
                .contains("COMMENT ON COLUMN TB_USER.USER_ID IS '사용자 아이디';");
    }

    @Test
    @DisplayName("MySQL/MariaDB : DECIMAL / DATETIME / 컬럼·테이블 COMMENT 옵션, COMMENT ON 없음")
    void mysql() {
        for (DbType db : List.of(DbType.MYSQL, DbType.MARIADB)) {
            String ddl = TableDdlGenerator.generate(db, table(), columns()).ddl();

            assertThat(ddl).as(db.name())
                    .contains("AMT     DECIMAL(12,2)")
                    .contains("REG_DT  DATETIME COMMENT '등록 일시'")
                    .contains("USER_NM VARCHAR(100) NOT NULL COMMENT '사용자''s 이름'")
                    .contains(") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='사용자';")
                    .doesNotContain("COMMENT ON");
        }
    }

    @Test
    @DisplayName("타입 변환 : BOOLEAN/CLOB/BLOB/INTEGER 를 DB 별 타입으로, 모르는 타입은 그대로")
    void typeMapping() {
        assertThat(type(DbType.ORACLE, "BOOLEAN")).isEqualTo("NUMBER(1)");
        assertThat(type(DbType.MYSQL, "BOOLEAN")).isEqualTo("TINYINT(1)");
        assertThat(type(DbType.POSTGRESQL, "CLOB")).isEqualTo("TEXT");
        assertThat(type(DbType.MARIADB, "BLOB")).isEqualTo("LONGBLOB");
        assertThat(type(DbType.ORACLE, "INTEGER")).isEqualTo("NUMBER(10)");
        assertThat(type(DbType.POSTGRESQL, "jsonb")).isEqualTo("JSONB");
        assertThat(type(DbType.MYSQL, "ENUM('A','B')")).isEqualTo("ENUM('A','B')");
    }

    @Test
    @DisplayName("문자열 길이가 없으면 DB 기본 길이로 만들고 경고한다 (PostgreSQL 은 길이 없는 VARCHAR)")
    void missingLength() {
        List<String> warnings = new ArrayList<>();
        ColumnInfo column = ColumnInfo.builder().columnName("MEMO").dataType("VARCHAR").build();

        assertThat(TableDdlGenerator.mapType(DbType.POSTGRESQL, column, warnings)).isEqualTo("VARCHAR");
        assertThat(TableDdlGenerator.mapType(DbType.ORACLE, column, warnings)).isEqualTo("VARCHAR2(4000)");
        assertThat(TableDdlGenerator.mapType(DbType.MYSQL, column, warnings)).isEqualTo("VARCHAR(255)");
        assertThat(warnings).hasSize(3).allSatisfy(w -> assertThat(w).startsWith("MEMO"));
    }

    @Test
    @DisplayName("기본값 : 숫자/키워드/함수/따옴표 값은 그대로, 나머지는 문자열로 감싼다")
    void defaultLiteral() {
        assertThat(TableDdlGenerator.defaultLiteral("0")).isEqualTo("0");
        assertThat(TableDdlGenerator.defaultLiteral("current_timestamp")).isEqualTo("current_timestamp");
        assertThat(TableDdlGenerator.defaultLiteral("NOW()")).isEqualTo("NOW()");
        assertThat(TableDdlGenerator.defaultLiteral("'N'")).isEqualTo("'N'");
        assertThat(TableDdlGenerator.defaultLiteral("N")).isEqualTo("'N'");
        assertThat(TableDdlGenerator.defaultLiteral("it's")).isEqualTo("'it''s'");
    }

    @Test
    @DisplayName("컬럼이 없거나 PK 가 없으면 경고한다")
    void warnings() {
        assertThat(TableDdlGenerator.generate(DbType.POSTGRESQL, table(), List.of()).warnings())
                .singleElement().asString().contains("컬럼이 없습니다");

        List<ColumnInfo> noPk = List.of(ColumnInfo.builder().columnName("A").dataType("CHAR").build());
        assertThat(TableDdlGenerator.generate(DbType.POSTGRESQL, table(), noPk).warnings()).contains("PK 컬럼이 없습니다.");
    }

    @Test
    @DisplayName("참조 테이블명이 있으면 참조 테이블의 단일 PK 로 FK 를 ALTER TABLE 로 만든다 (소문자 입력, 자기 참조 포함)")
    void foreignKey() {
        List<ColumnInfo> cols = List.of(
                ColumnInfo.builder().columnName("ORDER_ID").dataType("BIGINT").isPk(true).sortOrder(1).build(),
                ColumnInfo.builder().columnName("USER_ID").dataType("VARCHAR").maxLength(20).refTableName("tb_user").sortOrder(2).build(),
                ColumnInfo.builder().columnName("PARENT_ORDER_ID").dataType("BIGINT").refTableName("TB_ORDER").sortOrder(3).build());
        TableInfo order = TableInfo.builder().tableName("TB_ORDER").build();

        for (DbType db : DbType.values()) {
            TableDdlGenerator.Result result = TableDdlGenerator.generate(db, order, cols,
                    refs());

            assertThat(result.ddl()).as(db.name())
                    .contains("ALTER TABLE TB_ORDER ADD CONSTRAINT FK_TB_ORDER_USER_ID FOREIGN KEY (USER_ID) REFERENCES TB_USER (USER_ID);")
                    .contains("FOREIGN KEY (PARENT_ORDER_ID) REFERENCES TB_ORDER (ORDER_ID);");
            assertThat(result.ddl().indexOf("ALTER TABLE")).as("CREATE TABLE 뒤").isGreaterThan(result.ddl().indexOf("CREATE TABLE"));
            assertThat(result.warnings()).as(db.name()).isEmpty();
        }

        // Oracle : 제약명 30자 제한
        assertThat(TableDdlGenerator.generate(DbType.ORACLE, order, cols, refs()).ddl())
                .contains("ADD CONSTRAINT FK_TB_ORDER_PARENT_ORDER_ID FOREIGN KEY").doesNotContain("FK_TB_ORDER_PARENT_ORDER_ID_");
    }

    @Test
    @DisplayName("참조 테이블이 없거나 PK 가 없거나 복합 PK 면 FK 를 만들지 않고 경고한다")
    void foreignKeyWarnings() {
        List<ColumnInfo> cols = List.of(
                ColumnInfo.builder().columnName("ID").dataType("BIGINT").isPk(true).sortOrder(1).build(),
                ColumnInfo.builder().columnName("A_ID").dataType("BIGINT").refTableName("TB_NONE").sortOrder(2).build(),
                ColumnInfo.builder().columnName("B_ID").dataType("BIGINT").refTableName("TB_NOPK").sortOrder(3).build(),
                ColumnInfo.builder().columnName("C_ID").dataType("BIGINT").refTableName("TB_MULTI").sortOrder(4).build());

        TableDdlGenerator.Result result = TableDdlGenerator.generate(DbType.POSTGRESQL, table(), cols,
                Map.of("TB_NOPK", List.of(), "TB_MULTI", List.of(pk("K1", "BIGINT"), pk("K2", "BIGINT"))));

        assertThat(result.ddl()).doesNotContain("FOREIGN KEY");
        assertThat(result.warnings()).hasSize(3)
                .anySatisfy(w -> assertThat(w).startsWith("A_ID").contains("등록되어 있지 않아"))
                .anySatisfy(w -> assertThat(w).startsWith("B_ID").contains("PK 가 없어"))
                .anySatisfy(w -> assertThat(w).startsWith("C_ID").contains("복합키(K1, K2)"));
    }

    @Test
    @DisplayName("참조 컬럼과 참조 PK 의 타입이 다르면 FK 는 만들되 경고한다 (타입 다름 / 길이만 다름 구분)")
    void foreignKeyTypeMismatch() {
        TableInfo order = TableInfo.builder().tableName("TB_ORDER").build();
        List<ColumnInfo> cols = List.of(
                ColumnInfo.builder().columnName("ORDER_ID").dataType("BIGINT").isPk(true).sortOrder(1).build(),
                ColumnInfo.builder().columnName("USER_ID").dataType("NUMBER").maxLength(10).refTableName("TB_USER").sortOrder(2).build(),
                ColumnInfo.builder().columnName("CODE").dataType("VARCHAR").maxLength(10).refTableName("TB_CODE").sortOrder(3).build(),
                ColumnInfo.builder().columnName("GRP_ID").dataType("INTEGER").refTableName("TB_GRP").sortOrder(4).build());
        Map<String, List<ColumnInfo>> refs = Map.of(
                "TB_USER", List.of(pk("USER_ID", "VARCHAR(20)")),   // NUMERIC ↔ VARCHAR : 타입 다름
                "TB_CODE", List.of(pk("CODE", "VARCHAR(20)")),      // VARCHAR(10) ↔ VARCHAR(20) : 길이만 다름
                "TB_GRP", List.of(pk("GRP_ID", "BIGINT")));         // INTEGER ↔ BIGINT : 타입 다름

        TableDdlGenerator.Result result = TableDdlGenerator.generate(DbType.POSTGRESQL, order, cols, refs);

        assertThat(result.ddl()).contains("FK_TB_ORDER_USER_ID", "FK_TB_ORDER_CODE", "FK_TB_ORDER_GRP_ID"); // 경고만, FK 는 만든다
        assertThat(result.warnings()).hasSize(3)
                .anySatisfy(w -> assertThat(w).startsWith("USER_ID").contains("데이터 타입이 달라").contains("NUMERIC(10) ↔ TB_USER.USER_ID VARCHAR(20)"))
                .anySatisfy(w -> assertThat(w).startsWith("CODE").contains("길이/정밀도가 다릅니다"))
                .anySatisfy(w -> assertThat(w).startsWith("GRP_ID").contains("데이터 타입이 달라"));

        // 같은 타입이면 경고 없음 (VARCHAR 와 VARCHAR2 처럼 입력이 달라도 대상 DB 타입이 같으면 같다)
        List<ColumnInfo> same = List.of(
                ColumnInfo.builder().columnName("ORDER_ID").dataType("BIGINT").isPk(true).sortOrder(1).build(),
                ColumnInfo.builder().columnName("CODE").dataType("VARCHAR2(20)").refTableName("TB_CODE").sortOrder(2).build());
        assertThat(TableDdlGenerator.generate(DbType.ORACLE, order, same, Map.of("TB_CODE", List.of(pk("CODE", "VARCHAR(20)")))).warnings()).isEmpty();
    }

    private static Map<String, List<ColumnInfo>> refs() {
        return Map.of("TB_USER", List.of(pk("USER_ID", "VARCHAR(20)")), "TB_ORDER", List.of(pk("ORDER_ID", "BIGINT")));
    }

    private static ColumnInfo pk(String name, String dataType) {
        return ColumnInfo.builder().columnName(name).dataType(dataType).isPk(true).build();
    }

    private static String type(DbType db, String dataType) {
        return TableDdlGenerator.mapType(db, ColumnInfo.builder().columnName("C").dataType(dataType).build(), new ArrayList<>());
    }
}
