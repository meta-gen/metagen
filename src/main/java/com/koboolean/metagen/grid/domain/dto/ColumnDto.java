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
    private RowType type;

    public ColumnDto(String columnName, String column, ColumnType columnType) {
        this.columnName = columnName;
        this.column = column;
        this.columnType = columnType;
    }
}
