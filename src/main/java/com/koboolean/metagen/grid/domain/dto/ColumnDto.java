package com.koboolean.metagen.grid.domain.dto;

import com.koboolean.metagen.grid.enums.ColumnType;
import com.koboolean.metagen.grid.enums.RowType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ColumnDto {

    private String columnName;
    private String column;
    private ColumnType columnType;
    private RowType rowType;
    private Boolean isSearch;

    public ColumnDto(String columnName, String column, ColumnType columnType) {
        this.columnName = columnName;
        this.column = column;
        this.columnType = columnType;
        this.isSearch = false;
    }

    public ColumnDto(String columnName, String column, ColumnType columnType, Boolean isSearch) {
        this.columnName = columnName;
        this.column = column;
        this.columnType = columnType;
        this.isSearch = isSearch;
    }

    public ColumnDto(String columnName, String column, ColumnType columnType, RowType rowType) {
        this.columnName = columnName;
        this.column = column;
        this.columnType = columnType;
        this.rowType = rowType;
        this.isSearch = false;
    }
}
