package com.koboolean.metagen.security.domain.dto;

import com.koboolean.metagen.security.domain.entity.Resources;
import com.koboolean.metagen.security.domain.entity.Role;
import lombok.*;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ResourcesDto{
    private String id;
    private String resourceName;
    private String httpMethod;
    private int orderNum;
    private String resourceType;
    private String roleName;
    private String resourceDesc;
    private String role;

    public static ResourcesDto fromEntity(Resources resources) {

        String s = resources.getRoleSet().stream().findFirst().map(Role::getRoleDesc).orElse(null);
        String role = resources.getRoleSet().stream().findFirst().map(Role::getRoleName).orElse(null);

        return ResourcesDto.builder()
                .id(String.valueOf(resources.getId()))
                .resourceName(resources.getResourceName())
                .resourceDesc(resources.getResourceDesc())
                .httpMethod(resources.getHttpMethod())
                .orderNum(resources.getOrderNum())
                .resourceType(resources.getResourceType())
                .roleName(s)
                .role(role)
                .build();
    }

}
