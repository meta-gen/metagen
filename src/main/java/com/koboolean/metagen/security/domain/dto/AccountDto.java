package com.koboolean.metagen.security.domain.dto;

import com.koboolean.metagen.security.domain.entity.Account;
import com.koboolean.metagen.system.project.domain.dto.ProjectMemberDto;
import com.koboolean.metagen.system.project.domain.entity.ProjectMember;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

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
    private String roleName;
    private Long projectId;
    private boolean isPasswdCheck;

    public static AccountDto fromEntity(Account account) {

        return AccountDto.builder()
                .id(String.valueOf(account.getId()))
                .username(account.getUsername())
                .password(null)
                .name(account.getName())
                .build();
    }

}
