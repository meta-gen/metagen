package com.koboolean.metagen.system.access.service.impl;

import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.grid.enums.ColumnType;
import com.koboolean.metagen.grid.enums.RowType;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.domain.dto.ResourcesDto;
import com.koboolean.metagen.security.domain.entity.Resources;
import com.koboolean.metagen.security.domain.entity.Role;
import com.koboolean.metagen.security.exception.CustomException;
import com.koboolean.metagen.security.exception.domain.ErrorCode;
import com.koboolean.metagen.security.repository.ResourcesRepository;
import com.koboolean.metagen.security.repository.RoleRepository;
import com.koboolean.metagen.system.access.service.AccessService;
import com.koboolean.metagen.utils.AuthUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccessServiceImpl implements AccessService {

    private final RoleRepository roleRepository;
    private final ResourcesRepository resourcesRepository;

    @Override
    public List<ColumnDto> getAccessColumn() {

        ColumnDto columnDto = new ColumnDto("권한", "role", ColumnType.STRING, RowType.SELECT, false, false);

        columnDto.setOptions(roleRepository.findAll().stream().map(Role::getRoleName).collect(Collectors.toList()));

        return List.of(
                new ColumnDto("id", "id", ColumnType.STRING, false),
                new ColumnDto("리소스명", "resourceName", ColumnType.STRING, true),
                new ColumnDto("리소스설명", "resourceDesc", ColumnType.STRING, true),
                columnDto,
                new ColumnDto("권한명", "roleName", ColumnType.STRING, RowType.TEXT, false)
        );
    }

    @Override
    public Page<ResourcesDto> getAccessData(Pageable pageable, String searchColumn, String searchQuery, AccountDto accountDto) {

        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            // 검색어가 없을 경우 전체 조회
            return resourcesRepository.findAll(pageable).map(ResourcesDto::fromEntity);
        }

        Page<Resources> resources = null;

        searchQuery = "%" + searchQuery + "%";

        if(searchColumn.equals("resourceName")){
            resources = resourcesRepository.findAllByResourceNameLike(searchQuery, pageable);
        }else if(searchColumn.equals("resourceDesc")){
            resources = resourcesRepository.findAllByResourceDescLike(searchQuery, pageable);
        }

        return resources.map(ResourcesDto::fromEntity);
    }

    @Override
    @Transactional
    public void updateAccessRole(List<ResourcesDto> resourcesDtos) {
        if(!AuthUtil.isApprovalAdmin()){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        resourcesDtos.forEach(resourcesDto -> {
            Resources resource = resourcesRepository.findById(Long.valueOf(resourcesDto.getId())).orElse(null);

            if(resource != null){
                Role byRoleName = roleRepository.findByRoleName(resourcesDto.getRole());

                resource.setRoleSet(Set.of(byRoleName));
                byRoleName.getResourcesSet().add(resource); // 양방향 동기화
            }
        });

    }
}
