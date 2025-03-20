package com.koboolean.metagen.data.dictionary.service.impl;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardWordDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
import com.koboolean.metagen.data.dictionary.repository.StandardDomainRepository;
import com.koboolean.metagen.data.dictionary.repository.StandardTermRepository;
import com.koboolean.metagen.data.dictionary.repository.StandardWordRepository;
import com.koboolean.metagen.data.dictionary.service.DataDictionaryService;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DataDictionaryServiceImpl implements DataDictionaryService {

    private final StandardWordRepository standardWordRepository;
    private final StandardDomainRepository standardDomainRepository;
    private final StandardTermRepository standardTermRepository;


    @Override
    public List<ColumnDto> getStandardTermsColumn() {
        return List.of(
                new ColumnDto("id", "id"),
                new ColumnDto("제정차수", "revisionNumber"),
                new ColumnDto("표준용어명", "commonStandardTermName"),
                new ColumnDto("표준용어설명", "commonStandardTermDescription"),
                new ColumnDto("표준용어영문약어명", "commonStandardTermAbbreviation"),
                new ColumnDto("표준도메인명", "commonStandardDomainName"),
                new ColumnDto("허용값", "allowedValues"),
                new ColumnDto("저장 형식", "storageFormat"),
                new ColumnDto("표현 형식", "displayFormat"),
                new ColumnDto("행정표준코드명", "administrativeStandardCodeName"),
                new ColumnDto("소관기관명", "responsibleOrganization"),
                new ColumnDto("용어 이음동의어 목록", "synonymList")
        );
    }

    @Override
    public List<ColumnDto> getStandardDomainsColumn() {
        return List.of(
                new ColumnDto("id", "id"),
                new ColumnDto("제정차수", "revisionNumber"),
                new ColumnDto("표준도메인그룹명", "commonStandardDomainGroupName"),
                new ColumnDto("표준도메인분류명", "commonStandardDomainCategory"),
                new ColumnDto("표준도메인명", "commonStandardDomainName"),
                new ColumnDto("표준도메인설명", "commonStandardDomainDescription"),
                new ColumnDto("데이터타입", "dataType"),
                new ColumnDto("데이터길이", "dataLength"),
                new ColumnDto("데이터소수점길이", "dataDecimalLength"),
                new ColumnDto("저장 형식", "storageFormat"),
                new ColumnDto("표현 형식", "displayFormat"),
                new ColumnDto("단위", "unit"),
                new ColumnDto("허용값", "allowedValues")
        );
    }

    @Override
    public List<ColumnDto> getStandardWordsColumn() {
        return List.of(
                new ColumnDto("id", "id"),
                new ColumnDto("제정차수", "revisionNumber"),
                new ColumnDto("표준단어명", "commonStandardWordName"),
                new ColumnDto("표준단어영문약어명", "commonStandardWordAbbreviation"),
                new ColumnDto("표준단어 영문명", "commonStandardWordEnglishName"),
                new ColumnDto("표준단어 설명", "commonStandardWordDescription"),
                new ColumnDto("형식단어 여부", "isFormatWord"),
                new ColumnDto("표준도메인분류명", "commonStandardDomainCategory"),
                new ColumnDto("이음동의어 목록", "synonymList"),
                new ColumnDto("금칙어 목록", "restrictedWords")
        );
    }

    @Override
    public Page<StandardTermDto> getStandardTermsData(Pageable pageable, AccountDto accountDto) {
        Page<StandardTerm> allByProjectId = standardTermRepository.findAllByProjectId(accountDto.getProjectId(), pageable);
        return allByProjectId.map(StandardTermDto::fromEntity);
    }

    @Override
    public Page<StandardWordDto> getStandardWordsData(Pageable pageable, AccountDto accountDto) {
        Page<StandardWord> allByProjectId = standardWordRepository.findAllByProjectId(accountDto.getProjectId(), pageable);
        return allByProjectId.map(StandardWordDto::fromEntity);
    }

    @Override
    public Page<StandardDomainDto> getStandardDomainsData(Pageable pageable, AccountDto accountDto) {
        Page<StandardDomain> allByProjectId = standardDomainRepository.findAllByProjectId(accountDto.getProjectId(), pageable);
        return allByProjectId.map(StandardDomainDto::fromEntity);
    }
}
