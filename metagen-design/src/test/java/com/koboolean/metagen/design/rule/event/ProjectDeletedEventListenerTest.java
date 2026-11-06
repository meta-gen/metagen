package com.koboolean.metagen.design.rule.event;

import com.koboolean.metagen.core.project.domain.event.ProjectDeletedEvent;
import com.koboolean.metagen.design.rule.repository.TemplateRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProjectDeletedEventListenerTest {

    @Mock
    TemplateRepository templateRepository;

    @InjectMocks
    ProjectDeletedEventListener listener;

    @Test
    @DisplayName("프로젝트 삭제 이벤트를 받으면 해당 프로젝트의 템플릿을 삭제한다")
    void deleteTemplates() {
        listener.onProjectDeleted(new ProjectDeletedEvent(5L));

        verify(templateRepository).deleteByProjectId(5L);
    }
}
