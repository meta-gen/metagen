package com.koboolean.metagen.data.commoncolumn.service.impl;

import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.util.AuthUtil;
import com.koboolean.metagen.data.commoncolumn.domain.dto.CommonColumnDto;
import com.koboolean.metagen.data.commoncolumn.domain.entity.CommonColumn;
import com.koboolean.metagen.data.commoncolumn.repository.CommonColumnRepository;
import com.koboolean.metagen.data.commoncolumn.service.CommonColumnService;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import com.koboolean.metagen.data.dictionary.repository.StandardTermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommonColumnServiceImpl implements CommonColumnService {

    /** 공통 컬럼 세트 최대 항목 수 */
    static final int MAX_ITEMS = 50;

    private final CommonColumnRepository commonColumnRepository;
    private final StandardTermRepository standardTermRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CommonColumnDto> selectCommonColumns(AccountDto accountDto) {
        Long projectId = accountDto.getProjectId();
        List<CommonColumn> items = commonColumnRepository.findAllByProjectIdOrderBySortOrderAscIdAsc(projectId);
        Map<Long, StandardTerm> terms = findProjectTerms(projectId, items.stream().map(CommonColumn::getTermId).collect(Collectors.toSet()));

        return items.stream().map(item -> {
            StandardTerm term = terms.get(item.getTermId());
            return CommonColumnDto.builder()
                    .id(item.getId())
                    .termId(item.getTermId())
                    .sortOrder(item.getSortOrder())
                    .isNullable(toYN(item.getIsNullable()))
                    .isIndex(toYN(item.getIsIndex()))
                    .defaultValue(item.getDefaultValue())
                    .term(term == null ? null : StandardTermDto.fromEntity(term))
                    .build();
        }).toList();
    }

    @Override
    @Transactional
    public void replaceCommonColumns(AccountDto accountDto, List<CommonColumnDto> items) {
        if (!AuthUtil.isApprovalAvailable()) {
            throw new CustomException(ErrorCode.COMMON_COLUMN_PERMISSION_DENIED);
        }

        Long projectId = accountDto.getProjectId();
        List<CommonColumnDto> requested = items == null ? List.of() : items.stream().filter(Objects::nonNull).toList();
        if (requested.size() > MAX_ITEMS) {
            throw new CustomException(ErrorCode.COMMON_COLUMN_TOO_MANY, "공통 컬럼은 " + MAX_ITEMS + "개까지 등록할 수 있습니다.");
        }

        // 로그인 프로젝트의 용어만, 같은 컬럼명(영문약어명)은 한 번만
        Map<Long, StandardTerm> terms = findProjectTerms(projectId, requested.stream().map(CommonColumnDto::getTermId).filter(Objects::nonNull).collect(Collectors.toSet()));
        Set<String> abbreviations = new HashSet<>();
        for (CommonColumnDto item : requested) {
            StandardTerm term = item.getTermId() == null ? null : terms.get(item.getTermId());
            if (term == null) {
                throw new CustomException(ErrorCode.NOT_FOUND_TERM_DATA, "공통 컬럼의 표준용어를 찾을 수 없습니다. (ID " + item.getTermId() + ")");
            }
            if (!abbreviations.add(term.getCommonStandardTermAbbreviation())) {
                throw new CustomException(ErrorCode.COMMON_COLUMN_DUPLICATED, "공통 컬럼 세트에 같은 컬럼명이 중복되어 있습니다 : " + term.getCommonStandardTermAbbreviation());
            }
        }

        commonColumnRepository.deleteAll(commonColumnRepository.findAllByProjectIdOrderBySortOrderAscIdAsc(projectId));
        commonColumnRepository.flush();

        List<CommonColumn> saved = new ArrayList<>();
        for (int i = 0; i < requested.size(); i++) {
            CommonColumnDto item = requested.get(i);
            saved.add(CommonColumn.builder()
                    .projectId(projectId)
                    .termId(item.getTermId())
                    .sortOrder(i + 1)
                    .isNullable("Y".equalsIgnoreCase(item.getIsNullable()))
                    .isIndex("Y".equalsIgnoreCase(item.getIsIndex()))
                    .defaultValue(item.getDefaultValue() == null || item.getDefaultValue().isBlank() ? null : item.getDefaultValue().trim())
                    .build());
        }
        commonColumnRepository.saveAll(saved);
    }

    private Map<Long, StandardTerm> findProjectTerms(Long projectId, Set<Long> termIds) {
        if (termIds.isEmpty()) return Map.of();
        return standardTermRepository.findAllById(termIds).stream()
                .filter(term -> Objects.equals(term.getProjectId(), projectId)) // 다른 프로젝트 용어는 쓰지 않는다
                .collect(Collectors.toMap(StandardTerm::getId, Function.identity()));
    }

    private static String toYN(Boolean value) {
        return Boolean.TRUE.equals(value) ? "Y" : "N";
    }
}
