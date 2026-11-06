package com.koboolean.metagen.system.user.service;

import com.koboolean.metagen.common.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 시스템 관리 > 사용자 관리
 */
public interface UserManageService {

    List<ColumnDto> selectUserColumn();

    Page<AccountDto> selectUserData(Pageable pageable, AccountDto accountDto, String searchQuery, String searchColumn);

    void saveUser(List<AccountDto> accountDtos);

    void deleteUser(List<AccountDto> accountDtos);

    void saveUserPassword(AccountDto accountDto);
}
