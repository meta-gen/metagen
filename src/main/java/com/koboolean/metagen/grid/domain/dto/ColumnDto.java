package com.koboolean.metagen.grid.domain.dto;

import com.koboolean.metagen.grid.enums.ColumnType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ColumnDto {

    private String columnName;
    private String column;
    private ColumnType type;

    public ColumnDto(String columnName, String column) {
        this.columnName = columnName;
        this.column = column;
    }
}
