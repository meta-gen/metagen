package com.koboolean.metagen.home.service.impl;

import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.security.domain.entity.Account;
import com.koboolean.metagen.core.security.repository.UserRepository;
import com.koboolean.metagen.core.support.CoreFixtures;
import com.koboolean.metagen.home.domain.dto.DashboardDto;
import com.koboolean.metagen.home.domain.dto.RecentChangeDto;
import com.koboolean.metagen.home.repository.DashboardRepository;
import com.koboolean.metagen.operation.board.domain.dto.BoardViewDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceImplTest {

    @Mock
    UserRepository userRepository;
    @Mock
    DashboardRepository dashboardRepository;

    @InjectMocks
    DashboardServiceImpl service;

    @Test
    @DisplayName("로그인 프로젝트 기준으로 미승인 건수 + 최근 변경 5건 + 공지 3건을 모은다")
    void dashboard() {
        Account account = CoreFixtures.account(1L, "admin");
        List<RecentChangeDto> recent = List.of(mock(RecentChangeDto.class));
        List<BoardViewDto> notices = List.of(mock(BoardViewDto.class), mock(BoardViewDto.class));
        DashboardDto counts = DashboardDto.builder().pendingStandardCount(3).pendingTableCount(2).pendingColumnCount(1).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(account));
        when(dashboardRepository.findRecentChanges(7L, 5)).thenReturn(recent);
        when(dashboardRepository.findNotice(7L, 3, account)).thenReturn(notices);
        when(dashboardRepository.findDataCount(7L)).thenReturn(counts);

        DashboardDto result = service.selectDashboardData(AccountDto.builder().id("1").projectId(7L).build());

        assertThat(result.getPendingStandardCount()).isEqualTo(3);
        assertThat(result.getPendingTableCount()).isEqualTo(2);
        assertThat(result.getPendingColumnCount()).isEqualTo(1);
        assertThat(result.getRecentChanges()).isSameAs(recent);
        assertThat(result.getNotices()).isSameAs(notices);
    }
}
