package com.koboolean.metagen.data.table.ddl;

import com.koboolean.metagen.core.project.domain.enums.DbType;
import com.koboolean.metagen.data.column.domain.entity.ColumnInfo;
import com.koboolean.metagen.data.table.domain.entity.TableInfo;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 테이블/컬럼 설계 정보로 대상 DB 의 CREATE TABLE DDL 을 만든다. (테이블 관리 > 테이블 상세 > DDL 보기)
 *
 * - 컬럼은 정렬 순서(sortOrder) 순, PK 는 CONSTRAINT PK_테이블명, 고유값은 UK_테이블명_컬럼명, 인덱스는 IX_테이블명_컬럼명
 * - 참조 테이블명이 있는 컬럼은 FK_테이블명_컬럼명 (참조 테이블의 단일 PK 를 참조, ALTER TABLE 로 생성)
 * - NOT NULL : NULL 비허용 / PK / 필수 입력 컬럼
 * - 주석 : PostgreSQL·Oracle 은 COMMENT ON, MySQL·MariaDB 는 컬럼/테이블 COMMENT 옵션
 * - 데이터 타입은 공통 타입명(VARCHAR, NUMBER, DATE, TIMESTAMP, BOOLEAN, CLOB, BLOB …)을 DB 별 타입으로 바꾼다.
 *   길이가 없어 DB 기본값으로 정한 항목은 warnings 로 알려준다.
 */
public final class TableDdlGenerator {

    private TableDdlGenerator() {
    }

    /**
     * @param ddl      생성된 DDL 스크립트
     * @param warnings 확인이 필요한 항목 (길이 미지정, 컬럼 없음 등)
     */
    public record Result(String ddl, List<String> warnings) {}

    private static final Pattern TYPE_PATTERN = Pattern.compile("^\\s*([A-Za-z][A-Za-z0-9_ ]*?)\\s*(?:\\(\\s*([0-9]+)\\s*(?:,\\s*([0-9]+)\\s*)?\\))?\\s*$");

    // 따옴표 없이 그대로 쓸 기본값 (함수/키워드)
    private static final Set<String> RAW_DEFAULTS = Set.of(
            "NULL", "TRUE", "FALSE", "CURRENT_DATE", "CURRENT_TIME", "CURRENT_TIMESTAMP", "LOCALTIMESTAMP", "SYSDATE", "SYSTIMESTAMP");

    private static final Pattern NUMERIC_DEFAULT = Pattern.compile("^-?[0-9]+(\\.[0-9]+)?$");

    public static Result generate(DbType dbType, TableInfo table, List<ColumnInfo> columns) {
        return generate(dbType, table, columns, Map.of());
    }

    /**
     * @param refPkColumns 참조 테이블명(대문자) → 그 테이블의 PK 컬럼 (정렬 순서). 등록되지 않은 테이블은 키가 없다.
     *                     컬럼의 참조 테이블명으로 FK 를 만들고, 참조 컬럼과 PK 의 타입이 같은지 확인할 때 쓴다.
     */
    public static Result generate(DbType dbType, TableInfo table, List<ColumnInfo> columns, Map<String, List<ColumnInfo>> refPkColumns) {
        DbType db = DbType.orDefault(dbType);
        String tableName = table.getTableName();
        List<String> warnings = new ArrayList<>();

        List<ColumnInfo> ordered = columns.stream()
                .sorted(Comparator.comparing(ColumnInfo::getSortOrder, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        StringBuilder sql = new StringBuilder();
        sql.append("-- ").append(db.getLabel()).append(" DDL : ").append(tableName);
        if (notBlank(table.getTableKorName())) sql.append(" (").append(table.getTableKorName()).append(")");
        sql.append("\n");

        if (ordered.isEmpty()) {
            warnings.add("등록된 컬럼이 없습니다. 컬럼 관리에서 컬럼을 먼저 등록해주세요.");
            sql.append("-- 등록된 컬럼이 없습니다.\n");
            return new Result(sql.toString(), warnings);
        }

        List<String> lines = new ArrayList<>();
        int nameWidth = ordered.stream().mapToInt(c -> c.getColumnName().length()).max().orElse(0);

        for (ColumnInfo column : ordered) {
            StringBuilder line = new StringBuilder("    ")
                    .append(pad(column.getColumnName(), nameWidth)).append(" ")
                    .append(mapType(db, column, warnings));

            if (notBlank(column.getDefaultValue())) {
                line.append(" DEFAULT ").append(defaultLiteral(column.getDefaultValue()));
            }
            if (isNotNull(column)) {
                line.append(" NOT NULL");
            }
            if (isMySqlFamily(db) && notBlank(column.getColumnDesc())) {
                line.append(" COMMENT ").append(quote(column.getColumnDesc()));
            }
            lines.add(line.toString());
        }

        List<String> pkColumns = ordered.stream().filter(c -> Boolean.TRUE.equals(c.getIsPk())).map(ColumnInfo::getColumnName).toList();
        if (!pkColumns.isEmpty()) {
            lines.add("    CONSTRAINT " + constraintName(db, "PK_" + tableName) + " PRIMARY KEY (" + String.join(", ", pkColumns) + ")");
        } else {
            warnings.add("PK 컬럼이 없습니다.");
        }

        ordered.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsUnique()) && !Boolean.TRUE.equals(c.getIsPk()))
                .forEach(c -> lines.add("    CONSTRAINT " + constraintName(db, "UK_" + tableName + "_" + c.getColumnName()) + " UNIQUE (" + c.getColumnName() + ")"));

        sql.append("CREATE TABLE ").append(tableName).append(" (\n")
                .append(String.join(",\n", lines))
                .append("\n)");

        String tableComment = tableComment(table);
        if (isMySqlFamily(db)) {
            sql.append(" ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            if (tableComment != null) sql.append(" COMMENT=").append(quote(tableComment));
        }
        sql.append(";\n");

        // 인덱스 (PK/고유값 컬럼은 이미 인덱스가 있으므로 제외)
        List<ColumnInfo> indexColumns = ordered.stream()
                .filter(c -> Boolean.TRUE.equals(c.getIsIndex()) && !Boolean.TRUE.equals(c.getIsPk()) && !Boolean.TRUE.equals(c.getIsUnique()))
                .toList();
        if (!indexColumns.isEmpty()) {
            sql.append("\n");
            indexColumns.forEach(c -> sql.append("CREATE INDEX ").append(constraintName(db, "IX_" + tableName + "_" + c.getColumnName()))
                    .append(" ON ").append(tableName).append(" (").append(c.getColumnName()).append(");\n"));
        }

        // 외래키 : 참조 테이블의 단일 PK 를 참조한다. 테이블 생성 순서와 관계없이 실행할 수 있도록 ALTER TABLE 로 따로 만든다.
        List<String> foreignKeys = foreignKeys(db, tableName, ordered, refPkColumns, warnings);
        if (!foreignKeys.isEmpty()) {
            sql.append("\n").append(String.join("\n", foreignKeys)).append("\n");
        }

        // 주석 (PostgreSQL / Oracle)
        if (!isMySqlFamily(db)) {
            List<String> comments = new ArrayList<>();
            if (tableComment != null) comments.add("COMMENT ON TABLE " + tableName + " IS " + quote(tableComment) + ";");
            ordered.stream()
                    .filter(c -> notBlank(c.getColumnDesc()))
                    .forEach(c -> comments.add("COMMENT ON COLUMN " + tableName + "." + c.getColumnName() + " IS " + quote(c.getColumnDesc()) + ";"));
            if (!comments.isEmpty()) {
                sql.append("\n").append(String.join("\n", comments)).append("\n");
            }
        }

        return new Result(sql.toString(), warnings);
    }

    // ------------------------------------------------------------------ 외래키

    /**
     * 참조 테이블명이 있는 컬럼마다 FK 를 만든다.
     * - 참조 테이블이 등록되어 있지 않거나, PK 가 없거나, 복합 PK 이면 만들지 않고 경고한다.
     * - 자기 자신을 참조(계층 구조)할 수도 있다.
     * - 참조 컬럼과 참조 PK 의 타입(대상 DB 타입으로 바꾼 값)이 다르면 FK 는 만들되 경고한다.
     */
    static List<String> foreignKeys(DbType db, String tableName, List<ColumnInfo> columns,
                                    Map<String, List<ColumnInfo>> refPkColumns, List<String> warnings) {
        List<String> statements = new ArrayList<>();

        for (ColumnInfo column : columns) {
            if (!notBlank(column.getRefTableName())) continue;

            String refTable = column.getRefTableName().trim().toUpperCase(Locale.ROOT);
            List<ColumnInfo> pk = refPkColumns.get(refTable);
            String prefix = column.getColumnName() + " : 참조 테이블 " + refTable;

            if (pk == null) {
                warnings.add(prefix + " 이(가) 테이블 관리에 등록되어 있지 않아 FK 를 만들지 않았습니다.");
            } else if (pk.isEmpty()) {
                warnings.add(prefix + " 에 PK 가 없어 FK 를 만들지 않았습니다.");
            } else if (pk.size() > 1) {
                warnings.add(prefix + " 의 PK 가 복합키(" + String.join(", ", pk.stream().map(ColumnInfo::getColumnName).toList()) + ")라 FK 를 만들지 않았습니다.");
            } else if (refTable.equals(tableName) && pk.get(0).getColumnName().equals(column.getColumnName())) {
                warnings.add(prefix + " 의 PK 가 자기 자신이라 FK 를 만들지 않았습니다.");
            } else {
                ColumnInfo refPk = pk.get(0);
                statements.add("ALTER TABLE " + tableName
                        + " ADD CONSTRAINT " + constraintName(db, "FK_" + tableName + "_" + column.getColumnName())
                        + " FOREIGN KEY (" + column.getColumnName() + ") REFERENCES " + refTable + " (" + refPk.getColumnName() + ");");
                checkReferenceType(db, column, refTable, refPk, warnings);
            }
        }
        return statements;
    }

    /**
     * 참조 컬럼과 참조 PK 의 타입 비교 (대상 DB 타입 기준)
     * - 타입 자체가 다르면(VARCHAR ↔ NUMERIC, INTEGER ↔ BIGINT) DB 에 따라 FK 생성이 실패한다.
     * - 길이/정밀도만 다르면(VARCHAR(20) ↔ VARCHAR(30)) 생성은 되지만 값이 들어가지 않거나 잘릴 수 있다.
     */
    private static void checkReferenceType(DbType db, ColumnInfo column, String refTable, ColumnInfo refPk, List<String> warnings) {
        List<String> ignored = new ArrayList<>(); // 길이 미지정 경고는 컬럼 자체에서 이미 알려준다
        String type = mapType(db, column, ignored);
        String refType = mapType(db, refPk, ignored);
        if (type.equals(refType)) return;

        String pair = " (" + column.getColumnName() + " " + type + " ↔ " + refTable + "." + refPk.getColumnName() + " " + refType + ")";
        if (!baseType(type).equals(baseType(refType))) {
            warnings.add(column.getColumnName() + " : 참조 PK 와 데이터 타입이 달라 FK 생성이 실패할 수 있습니다." + pair);
        } else {
            warnings.add(column.getColumnName() + " : 참조 PK 와 길이/정밀도가 다릅니다. 값이 들어가지 않거나 잘리지 않도록 맞추는 것을 권장합니다." + pair);
        }
    }

    private static String baseType(String type) {
        int paren = type.indexOf('(');
        return (paren < 0 ? type : type.substring(0, paren)).trim();
    }

    // ------------------------------------------------------------------ 데이터 타입

    /**
     * 공통 타입명을 DB 별 타입으로 바꾼다. 타입에 길이가 함께 적혀 있으면(VARCHAR(100)) 그 값을 먼저 쓴다.
     */
    static String mapType(DbType db, ColumnInfo column, List<String> warnings) {
        String raw = column.getDataType() == null ? "" : column.getDataType().trim();
        Matcher matcher = TYPE_PATTERN.matcher(raw);

        String base;
        Integer length = column.getMaxLength();
        Integer precision = column.getPrecision() == null ? null : toInt(column.getPrecision());
        Integer scale = column.getScale();

        if (raw.isEmpty()) {
            warnings.add(column.getColumnName() + " : 데이터 타입이 없어 문자열(VARCHAR)로 생성했습니다.");
            base = "VARCHAR";
        } else if (matcher.matches()) {
            base = matcher.group(1).trim().toUpperCase(Locale.ROOT).replaceAll("\\s+", " ");
            if (matcher.group(2) != null) {
                int first = Integer.parseInt(matcher.group(2));
                length = first;
                precision = first;
                if (matcher.group(3) != null) scale = Integer.parseInt(matcher.group(3));
            }
        } else {
            return raw; // 해석할 수 없는 타입은 그대로 쓴다 (예: ENUM('A','B'))
        }

        return switch (base) {
            case "VARCHAR", "VARCHAR2", "NVARCHAR", "NVARCHAR2", "CHARACTER VARYING", "STRING" -> {
                Integer len = length != null ? length : defaultLength(db, column, warnings);
                yield switch (db) {
                    case ORACLE -> "VARCHAR2(" + len + ")";
                    default -> len == null ? "VARCHAR" : "VARCHAR(" + len + ")";
                };
            }
            case "CHAR", "NCHAR", "CHARACTER" -> "CHAR(" + (length != null ? length : 1) + ")";
            case "NUMBER", "NUMERIC", "DECIMAL", "DEC" -> {
                Integer p = precision != null ? precision : length;
                String args = p == null ? "" : "(" + p + (scale != null && scale > 0 ? "," + scale : "") + ")";
                yield switch (db) {
                    case ORACLE -> "NUMBER" + args;
                    case POSTGRESQL -> "NUMERIC" + args;
                    default -> "DECIMAL" + args;
                };
            }
            case "INT", "INTEGER", "INT4" -> db == DbType.ORACLE ? "NUMBER(10)" : (isMySqlFamily(db) ? "INT" : "INTEGER");
            case "BIGINT", "INT8", "LONG" -> db == DbType.ORACLE ? "NUMBER(19)" : "BIGINT";
            case "SMALLINT", "INT2" -> db == DbType.ORACLE ? "NUMBER(5)" : "SMALLINT";
            case "FLOAT", "DOUBLE", "DOUBLE PRECISION", "REAL" -> switch (db) {
                case ORACLE -> "BINARY_DOUBLE";
                case POSTGRESQL -> "DOUBLE PRECISION";
                default -> "DOUBLE";
            };
            case "DATE" -> "DATE";
            case "DATETIME" -> switch (db) {
                case ORACLE -> "DATE";
                case POSTGRESQL -> "TIMESTAMP";
                default -> "DATETIME";
            };
            case "TIMESTAMP" -> isMySqlFamily(db) ? "DATETIME" : "TIMESTAMP";
            case "TIME" -> db == DbType.ORACLE ? "DATE" : "TIME";
            case "BOOLEAN", "BOOL", "BIT" -> switch (db) {
                case ORACLE -> "NUMBER(1)";
                case POSTGRESQL -> "BOOLEAN";
                default -> "TINYINT(1)";
            };
            case "CLOB", "TEXT", "LONGTEXT", "NCLOB" -> switch (db) {
                case ORACLE -> "CLOB";
                case POSTGRESQL -> "TEXT";
                default -> "LONGTEXT";
            };
            case "BLOB", "BYTEA", "LONGBLOB", "BINARY", "VARBINARY" -> switch (db) {
                case ORACLE -> "BLOB";
                case POSTGRESQL -> "BYTEA";
                default -> "LONGBLOB";
            };
            default -> raw.toUpperCase(Locale.ROOT); // 알 수 없는 타입은 입력 그대로 (DB 고유 타입 등)
        };
    }

    /**
     * 문자열 길이가 없을 때 : PostgreSQL 은 길이 없는 VARCHAR, Oracle 4000, MySQL/MariaDB 255
     */
    private static Integer defaultLength(DbType db, ColumnInfo column, List<String> warnings) {
        Integer len = switch (db) {
            case POSTGRESQL -> null;
            case ORACLE -> 4000;
            default -> 255;
        };
        warnings.add(column.getColumnName() + " : 최대 길이가 없어 " + (len == null ? "길이 없는 VARCHAR" : "VARCHAR(" + len + ")") + " 로 생성했습니다.");
        return len;
    }

    // ------------------------------------------------------------------ 보조

    private static boolean isNotNull(ColumnInfo column) {
        return Boolean.TRUE.equals(column.getIsPk())
                || Boolean.TRUE.equals(column.getIsRequired())
                || Boolean.FALSE.equals(column.getIsNullable());
    }

    /**
     * 기본값 : 숫자·키워드·함수 호출·이미 따옴표로 감싼 값은 그대로, 나머지는 문자열로 감싼다.
     */
    static String defaultLiteral(String value) {
        String v = value.trim();
        if (NUMERIC_DEFAULT.matcher(v).matches()
                || RAW_DEFAULTS.contains(v.toUpperCase(Locale.ROOT))
                || v.contains("(")
                || (v.length() >= 2 && v.startsWith("'") && v.endsWith("'"))) {
            return v;
        }
        return quote(v);
    }

    private static String tableComment(TableInfo table) {
        if (notBlank(table.getTableKorName())) return table.getTableKorName().trim();
        if (notBlank(table.getTableDescription())) return table.getTableDescription().trim();
        return null;
    }

    /**
     * 제약/인덱스 이름 : Oracle(12.1 이하 30자), MySQL(64자) 제한을 넘으면 자른다.
     */
    private static String constraintName(DbType db, String name) {
        int max = switch (db) {
            case ORACLE -> 30;
            case POSTGRESQL -> 63;
            default -> 64;
        };
        return name.length() > max ? name.substring(0, max) : name;
    }

    private static boolean isMySqlFamily(DbType db) {
        return db == DbType.MYSQL || db == DbType.MARIADB;
    }

    private static String quote(String value) {
        return "'" + value.trim().replace("'", "''") + "'";
    }

    private static String pad(String value, int width) {
        return value.length() >= width ? value : value + " ".repeat(width - value.length());
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }

    private static Integer toInt(BigDecimal value) {
        try {
            return value.intValueExact();
        } catch (ArithmeticException e) {
            return value.intValue();
        }
    }
}
