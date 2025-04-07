package com.koboolean.metagen.security.domain.dto;

import com.koboolean.metagen.security.domain.entity.Account;
import com.koboolean.metagen.security.domain.entity.Role;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Accessors(chain = true)
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class AccountDto {
    private String id;
    private String username;
    private String name;
    private String password;
    private String currentPassword;
    private List<String> roles;
    private String role;
    private String roleName;
    private Long projectId;
    private boolean isPasswdCheck;
    private String resetButton;

    public static AccountDto fromEntity(Account account) {

        return AccountDto.builder()
                .id(String.valueOf(account.getId()))
                .username(account.getUsername())
                .password(null)
                .name(account.getName())
                .roles(account.getUserRoles().stream().map(Role::getRoleName).collect(Collectors.toList()))
                .role(account.getUserRoles().stream().findFirst().get().getRoleName())
                .roleName(account.getUserRoles().stream().findFirst().get().getRoleDesc())
                .resetButton("초기화")
                .build();
    }

}
