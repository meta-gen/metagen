package com.koboolean.metagen.data.dictionary.service;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardWordDto;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.logs.domain.dto.LogsDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DataDictionaryService {
    List<ColumnDto> getStandardTermsColumn();
    List<ColumnDto> getStandardWordsColumn();
    List<ColumnDto> getStandardDomainsColumn();

    Page<StandardTermDto> getStandardTermsData(Pageable pageable, AccountDto accountDto);
    Page<StandardWordDto> getStandardWordsData(Pageable pageable, AccountDto accountDto);
    Page<StandardDomainDto> getStandardDomainsData(Pageable pageable, AccountDto accountDto);

}
