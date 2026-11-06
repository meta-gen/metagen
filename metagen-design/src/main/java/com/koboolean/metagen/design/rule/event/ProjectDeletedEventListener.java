package com.koboolean.metagen.design.rule.event;

import com.koboolean.metagen.design.rule.repository.TemplateRepository;
import com.koboolean.metagen.core.project.domain.event.ProjectDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 프로젝트 삭제 시 해당 프로젝트의 템플릿(및 cascade 된 코드규칙)을 삭제한다.
 */
@Component
@RequiredArgsConstructor
public class ProjectDeletedEventListener {

    private final TemplateRepository templateRepository;

    @EventListener
    public void onProjectDeleted(ProjectDeletedEvent event) {
        templateRepository.deleteByProjectId(event.projectId());
    }
}
