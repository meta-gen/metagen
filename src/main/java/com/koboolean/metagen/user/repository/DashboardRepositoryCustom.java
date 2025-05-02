package com.koboolean.metagen.user.repository;

import com.koboolean.metagen.board.domain.dto.BoardDto;
import com.koboolean.metagen.home.domain.dto.DashboardDto;
import com.koboolean.metagen.home.domain.dto.RecentChangeDto;

import java.util.List;

public interface DashboardRepositoryCustom {

    List<RecentChangeDto> findRecentChanges(Long projectId, int limit);
    List<BoardDto> findNotice(Long projectId, int limit);

    DashboardDto findDataCount(Long projectId);
}
