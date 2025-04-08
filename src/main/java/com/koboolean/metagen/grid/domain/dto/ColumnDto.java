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
}
