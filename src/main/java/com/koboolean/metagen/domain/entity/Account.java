package com.koboolean.metagen.domain.entity;

import com.koboolean.metagen.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Account extends BaseEntity implements Serializable {
    @Id
    @GeneratedValue
    private Long id;

    @Column
    private String username;

    @Column
    private String password;

    @Column
    private String name;

    /**
     * 패스워드 변경여부 - 기본적으로 admin에서 접근했을 때 패스워드 변경을 먼저 할 수 있도록 내정보로 redirect한다
     * admin 이외의 계정에서는 true를 반환하여 원하는 위치로 이동할 수 있도록 작업해준다.
     */
    @Column(name = "IS_PASSWORD_CHECK")
    private boolean isPasswdCheck;

    @ManyToMany(fetch = FetchType.LAZY, cascade={CascadeType.MERGE})
    @JoinTable(name = "account_roles", joinColumns = { @JoinColumn(name = "account_id") }, inverseJoinColumns = {
            @JoinColumn(name = "role_id") })
    @ToString.Exclude
    private Set<Role> userRoles = new HashSet<>();
}

