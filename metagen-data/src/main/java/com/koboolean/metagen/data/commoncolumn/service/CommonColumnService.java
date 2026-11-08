package com.koboolean.metagen.data.commoncolumn.service;

import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.data.commoncolumn.domain.dto.CommonColumnDto;

import java.util.List;

public interface CommonColumnService {

    /**
     * 로그인 프로젝트의 공통 컬럼 세트 (순서대로)
     */
    List<CommonColumnDto> selectCommonColumns(AccountDto accountDto);

    /**
     * 공통 컬럼 세트를 통째로 바꾼다. (프로젝트 관리자 / ADMIN / MANAGER 만)
     */
    void replaceCommonColumns(AccountDto accountDto, List<CommonColumnDto> items);
}
