package com.koboolean.metagen.user.repository.impl;

import com.koboolean.metagen.security.domain.entity.Project;
import com.koboolean.metagen.security.domain.entity.QProject;
import com.koboolean.metagen.security.domain.entity.QProjectMember;
import com.koboolean.metagen.user.repository.ProjectRepositoryCustom;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Project> findProjectsByUsername(String username) {
        QProject project = QProject.project;
        QProjectMember projectMember = QProjectMember.projectMember;

        return queryFactory
                .selectFrom(project)
                .join(projectMember).on(project.id.eq(projectMember.projectId))
                .where(projectMember.account.username.eq(username))
                .where(project.isActive.eq(true))
                .where(projectMember.isActive.eq(true))
                .fetch();
    }
}
