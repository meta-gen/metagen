package com.koboolean.metagen.security.domain.dto;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@Builder
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

}
