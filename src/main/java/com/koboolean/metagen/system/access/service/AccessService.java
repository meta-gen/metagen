package com.koboolean.metagen.system.access.service;

import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.domain.dto.ResourcesDto;
import com.koboolean.metagen.system.project.domain.dto.ProjectMemberDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AccessService {
    List<ColumnDto> getAccessColumn();

    Page<ResourcesDto> getAccessData(Pageable pageable, String searchColumn, String searchQuery, AccountDto accountDto);

    void updateAccessRole(List<ResourcesDto> resourcesDtos);
}
