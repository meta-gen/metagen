package com.koboolean.metagen.home.domain.dto;


import com.koboolean.metagen.board.domain.dto.BoardDto;
import lombok.*;

import java.util.List;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DashboardDto {

    private int pendingStandardCount;
    private int pendingTableCount;
    private int pendingColumnCount;

    private List<BoardDto> notices;
    private List<RecentChangeDto> recentChanges;

}
