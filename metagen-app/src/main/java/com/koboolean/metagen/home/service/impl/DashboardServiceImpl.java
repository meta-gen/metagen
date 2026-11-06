package com.koboolean.metagen.home.service.impl;

import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.security.domain.entity.Account;
import com.koboolean.metagen.core.security.repository.UserRepository;
import com.koboolean.metagen.home.domain.dto.DashboardDto;
import com.koboolean.metagen.home.domain.dto.RecentChangeDto;
import com.koboolean.metagen.home.repository.DashboardRepository;
import com.koboolean.metagen.home.service.DashboardService;
import com.koboolean.metagen.operation.board.domain.dto.BoardViewDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final UserRepository userRepository;
    private final DashboardRepository dashboardRepository;

    @Override
    public DashboardDto selectDashboardData(AccountDto accountDto) {

        Long projectId = accountDto.getProjectId();

        Account account = userRepository.findById(Long.parseLong(accountDto.getId())).orElse(null);


        List<RecentChangeDto> recentChanges = dashboardRepository.findRecentChanges(projectId, 5);
        List<BoardViewDto> notice = dashboardRepository.findNotice(projectId, 3, account);

        DashboardDto dashboardDto = dashboardRepository.findDataCount(projectId);

        dashboardDto.setRecentChanges(recentChanges);
        dashboardDto.setNotices(notice);

        return dashboardDto;
    }
}
