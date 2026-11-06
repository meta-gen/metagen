package com.koboolean.metagen.core.support;

import com.koboolean.metagen.core.project.domain.entity.Project;
import com.koboolean.metagen.core.project.domain.entity.ProjectMember;
import com.koboolean.metagen.core.security.domain.entity.Account;
import com.koboolean.metagen.core.security.domain.entity.Role;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * core 엔티티 생성 헬퍼.
 * Lombok @Builder 는 필드 초기값을 무시하므로 컬렉션 필드는 여기서 명시적으로 채운다.
 */
public final class CoreFixtures {

    private CoreFixtures() {
    }

    public static Role role(Long id, String roleName) {
        return Role.builder()
                .id(id)
                .roleName(roleName)
                .roleDesc(roleName + "_DESC")
                .accounts(new HashSet<>())
                .build();
    }

    public static Account account(Long id, String username, Role... roles) {
        Account account = Account.builder()
                .id(id)
                .username(username)
                .name(username + "_name")
                .password("encoded")
                .isActive(true)
                .isPasswdCheck(true) // 일반 계정 (초기 비밀번호 변경 완료)
                .userRoles(new HashSet<>(Set.of(roles)))
                .projectMember(new ArrayList<>())
                .build();
        for (Role role : roles) {
            role.getAccounts().add(account);
        }
        return account;
    }

    /**
     * 프로젝트 관리자(manager)를 멤버로 포함한 프로젝트
     */
    public static Project project(Long id, Account manager) {
        Project project = Project.builder()
                .id(id)
                .projectName("프로젝트" + id)
                .account(manager)
                .isActive(true)
                .isAutoActive(false)
                .isDicAbbrUsed(false)
                .projectMember(new ArrayList<>())
                .build();
        addMember(project, manager, true);
        return project;
    }

    public static ProjectMember addMember(Project project, Account account, boolean active) {
        ProjectMember member = ProjectMember.builder()
                .id((long) (project.getProjectMember().size() + 100))
                .project(project)
                .account(account)
                .isActive(active)
                .build();
        project.getProjectMember().add(member);
        return member;
    }
}
