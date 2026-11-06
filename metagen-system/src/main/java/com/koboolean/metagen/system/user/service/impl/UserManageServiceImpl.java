package com.koboolean.metagen.system.user.service.impl;

import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.common.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.common.grid.enums.ColumnType;
import com.koboolean.metagen.common.grid.enums.RowType;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.security.domain.entity.Account;
import com.koboolean.metagen.core.security.domain.entity.Role;
import com.koboolean.metagen.core.security.repository.RoleRepository;
import com.koboolean.metagen.core.security.repository.UserRepository;
import com.koboolean.metagen.system.user.service.UserManageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserManageServiceImpl implements UserManageService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public List<ColumnDto> selectUserColumn() {

        ColumnDto columnDto = new ColumnDto("권한", "role", ColumnType.STRING, RowType.SELECT, false);

        columnDto.setOptions(roleRepository.findAll().stream().filter(role -> !role.getRoleName().equals("ROLE_ANONYMOUS")).map(Role::getRoleName).collect(Collectors.toList()));

        return List.of(
                new ColumnDto("","id", ColumnType.NUMBER, RowType.CHECKBOX),
                new ColumnDto("사용자 ID","username", ColumnType.STRING, true),
                new ColumnDto("사용자명","name", ColumnType.STRING, true),
                columnDto,
                new ColumnDto("권한 명", "roleName", ColumnType.STRING, RowType.TEXT, false),
                new ColumnDto("비밀번호 초기화", "resetButton", ColumnType.STRING, RowType.BUTTON, false)
        );
    }

    @Override
    public Page<AccountDto> selectUserData(Pageable pageable, AccountDto accountDto, String searchQuery, String searchColumn) {
        return userRepository.findAllByIsActive(true, pageable).map(AccountDto::fromEntity);
    }

    @Override
    @Transactional
    public void saveUser(List<AccountDto> accountDtos) {
//        if(!AuthUtil.isApprovalAvailable()){
//            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
//        }

        accountDtos.forEach(accountDto -> {

            Account account = userRepository.findById(Long.parseLong(accountDto.getId())).orElse(null);

            if(account != null){
                Role role = account.getUserRoles().stream().findFirst().get();

                // 권한이 변경된것을 확인하였으므로, 해당 권한으로 UPDATE한다.
                if(!role.getRoleName().equals(accountDto.getRole())){

                    if(accountDto.getId().equals("0")){
                        // admin 계정은 권한 변경이 불가능하다.
                        throw new CustomException(ErrorCode.MANAGER_NON_DELETED);
                    }

                    Role byRoleName = roleRepository.findByRoleName(accountDto.getRole());
                    account.setUserRoles(Set.of(byRoleName));
                }
            }
        });
    }

    @Override
    @Transactional
    public void deleteUser(List<AccountDto> accountDtos) {
//        if(!AuthUtil.isApprovalAvailable()){
//            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
//        }

        Role adminRole = roleRepository.findByRoleName("ROLE_ADMIN");

        accountDtos.forEach(accountDto -> {
            Account account = userRepository.findById(Long.parseLong(accountDto.getId()))
                    .orElse(null);

            if(account != null){
                boolean isAdmin = account.getUserRoles().contains(adminRole);

                if (isAdmin) {
                    long activeAdminCount = adminRole.getAccounts().stream()
                            .filter(Account::getIsActive)
                            .distinct()
                            .count();

                    if (activeAdminCount <= 1) {
                        throw new CustomException(ErrorCode.MANAGER_NON_DELETED);
                    }
                }

                account.setIsActive(false); // soft delete
            }
        });
    }

    @Override
    @Transactional
    public void saveUserPassword(AccountDto accountDto) {
//        if(!AuthUtil.isApprovalAvailable()){
//            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
//        }

        Account account = userRepository.findById(Long.parseLong(accountDto.getId())).orElse(null);

        if(account != null){
            account.setPassword(passwordEncoder.encode(accountDto.getUsername()));
            account.setPasswdCheck(false);
        }
    }
}
