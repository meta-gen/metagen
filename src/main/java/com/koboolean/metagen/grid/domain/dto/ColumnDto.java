package com.koboolean.metagen.grid.domain.dto;

import com.koboolean.metagen.grid.enums.ColumnType;
import com.koboolean.metagen.grid.enums.RowType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class ColumnDto {

    private String columnName;
    private String column;
    private ColumnType columnType;
    private RowType rowType;
    private List<String> options;
    private Boolean isSearch;
    private Boolean isFilter;
    private Boolean isVisible = true; // false : 그리드에 표시하지 않음 (검색 조건으로는 사용 가능)

    public ColumnDto(String columnName, String column, ColumnType columnType) {
        this.columnName = columnName;
        this.column = column;
        this.columnType = columnType;
        this.isSearch = false;
        this.isFilter = true;
    }

    public ColumnDto(String columnName, String column, ColumnType columnType, Boolean isSearch) {
        this.columnName = columnName;
        this.column = column;
        this.columnType = columnType;
        this.isSearch = isSearch;
        this.isFilter = true;
    }

    public ColumnDto(String columnName, String column, ColumnType columnType, RowType rowType) {
        this.columnName = columnName;
        this.column = column;
        this.columnType = columnType;
        this.rowType = rowType;
        this.isSearch = false;
        this.isFilter = true;
    }

    public ColumnDto(String columnName, String column, ColumnType columnType, RowType rowType, Boolean isFilter) {
        this.columnName = columnName;
        this.column = column;
        this.columnType = columnType;
        this.rowType = rowType;
        this.isSearch = false;
        this.isFilter = isFilter;
    }

    public ColumnDto(String columnName, String column, ColumnType columnType, RowType rowType, Boolean isSearch, Boolean isFilter) {
        this.columnName = columnName;
        this.column = column;
        this.columnType = columnType;
        this.rowType = rowType;
        this.isSearch = isSearch;
        this.isFilter = isFilter;
    }

    /**
     * 그리드에는 표시하지 않고 검색 조건으로만 사용하는 컬럼으로 설정한다.
     */
    public ColumnDto hidden() {
        this.isVisible = false;
        return this;
    }
}
