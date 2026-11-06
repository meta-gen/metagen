package com.koboolean.metagen.home.repository;

import com.koboolean.metagen.operation.board.domain.dto.BoardDto;
import com.koboolean.metagen.operation.board.domain.dto.BoardViewDto;
import com.koboolean.metagen.home.domain.dto.DashboardDto;
import com.koboolean.metagen.home.domain.dto.RecentChangeDto;
import com.koboolean.metagen.core.security.domain.entity.Account;

import java.util.List;

public interface DashboardRepositoryCustom {

    List<RecentChangeDto> findRecentChanges(Long projectId, int limit);
    List<BoardViewDto> findNotice(Long projectId, int limit, Account account);

    DashboardDto findDataCount(Long projectId);
}
