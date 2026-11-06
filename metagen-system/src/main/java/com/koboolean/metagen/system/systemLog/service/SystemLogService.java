package com.koboolean.metagen.system.systemLog.service;

import com.koboolean.metagen.common.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.core.logs.domain.dto.LogsDto;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface SystemLogService {

    List<ColumnDto> getSystemLogColumn();

    Page<LogsDto> getSystemLogData(Pageable pageable, AccountDto accountDto, String searchQuery, String searchColumn);
}
