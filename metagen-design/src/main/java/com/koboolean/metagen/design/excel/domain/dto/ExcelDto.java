package com.koboolean.metagen.design.excel.domain.dto;

import com.koboolean.metagen.design.code.domain.dto.CodeRuleDetailDto;
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
