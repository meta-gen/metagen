package com.koboolean.metagen.data.dictionary.service;

import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import com.koboolean.metagen.data.dictionary.repository.StandardDomainRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StandardDomainServiceTest {

    @Mock
    StandardDomainRepository standardDomainRepository;

    @InjectMocks
    StandardDomainService service;

    private final AccountDto login = AccountDto.builder().id("1").projectId(1L).build();

    private static StandardDomain domain(boolean approved) {
        return StandardDomain.builder()
                .id(1L)
                .projectId(1L)
                .commonStandardDomainName("번호N10")
                .isApproval(approved)
                .standardTerms(new ArrayList<>())
                .build();
    }

    private static List<StandardDomainDto> ids(Long... ids) {
        return java.util.Arrays.stream(ids).map(id -> StandardDomainDto.builder().id(id).build()).toList();
    }

    @Test
    @DisplayName("승인된 도메인은 삭제할 수 없다")
    void approved() {
        when(standardDomainRepository.findByIdAndProjectId(1L, 1L)).thenReturn(domain(true));

        assertThatThrownBy(() -> service.deleteDataDictionaryStandardDomains(ids(1L), login))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode").isEqualTo(ErrorCode.APPROVED_DATA_CANNOT_BE_DELETED);
    }

    @Test
    @DisplayName("표준용어가 참조 중인 도메인은 삭제할 수 없다")
    void usedByTerm() {
        StandardDomain domain = domain(false);
        domain.getStandardTerms().add(new StandardTerm());
        when(standardDomainRepository.findByIdAndProjectId(1L, 1L)).thenReturn(domain);

        assertThatThrownBy(() -> service.deleteDataDictionaryStandardDomains(ids(1L), login))
                .extracting("errorCode").isEqualTo(ErrorCode.RELATION_EXISTS);
        verify(standardDomainRepository, never()).delete(any());
    }

    @Test
    @DisplayName("미승인 + 미사용 도메인은 삭제한다")
    void delete() {
        StandardDomain domain = domain(false);
        when(standardDomainRepository.findByIdAndProjectId(1L, 1L)).thenReturn(domain);

        service.deleteDataDictionaryStandardDomains(ids(1L), login);

        verify(standardDomainRepository).delete(domain);
    }

    @Test
    @DisplayName("승인 처리는 로그인 프로젝트의 도메인 상태를 변경한다")
    void approval() {
        StandardDomain domain = domain(false);
        when(standardDomainRepository.findByIdAndProjectId(1L, 1L)).thenReturn(domain);

        service.approvalStandardDomains(ids(1L), login, true);

        assertThat(domain.getIsApproval()).isTrue();
    }
}
