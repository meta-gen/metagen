package com.koboolean.metagen.excel.domain.dto;

import com.koboolean.metagen.data.code.domain.dto.CodeRuleDetailDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExcelDto {

    private String formatType;
    private String formatText;

    private String type;

    private String column;
    private String searchValue;

    private List<CodeRuleDetailDto> rules;
}
