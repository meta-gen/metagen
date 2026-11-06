package com.koboolean.metagen.system.systemLog.service.impl;

import com.koboolean.metagen.common.grid.enums.ColumnType;
import com.koboolean.metagen.common.grid.enums.RowType;
import com.koboolean.metagen.core.logs.domain.dto.LogsDto;
import com.koboolean.metagen.core.logs.domain.entity.Logs;
import com.koboolean.metagen.core.logs.repository.LogsRepository;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.common.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.system.systemLog.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl implements SystemLogService {

    private final LogsRepository logsRepository;

    @Override
    public List<ColumnDto> getSystemLogColumn() {
        return List.of(
                new ColumnDto("id","id", ColumnType.NUMBER)
                , new ColumnDto("요청 URL","logUrl", ColumnType.STRING, true)
                , new ColumnDto("메소드","method", ColumnType.STRING, true)
                , new ColumnDto("IP","ip", ColumnType.STRING, true)
                , new ColumnDto("사용자 ID","username", ColumnType.STRING, true)
                , new ColumnDto("권한","roleName", ColumnType.STRING)
                , new ColumnDto("응답 HTTP 상태코드","statusCode", ColumnType.STRING, true)
                , new ColumnDto("요청 발생시간","timestamp", ColumnType.STRING)
                , new ColumnDto("상세보기","detailButton", ColumnType.STRING, RowType.BUTTON, false)
                // 요청/응답 데이터, 에러 메시지, 브라우저 정보는 상세보기 다이얼로그에서 확인 (검색 조건으로는 유지)
                , new ColumnDto("요청 데이터","requestBody", ColumnType.STRING, true).hidden()
                , new ColumnDto("응답 데이터","responseBody", ColumnType.STRING, true).hidden()
                , new ColumnDto("에러 메시지","errorMessage", ColumnType.STRING, true).hidden()
                , new ColumnDto("브라우저 정보","userAgent", ColumnType.STRING).hidden()
        );
    }

    @Override
    public Page<LogsDto> getSystemLogData(Pageable pageable, AccountDto accountDto, String searchQuery, String searchColumn) {

        Long projectId = accountDto.getProjectId();

        if (projectId == null) {
            throw new IllegalArgumentException("projectId 값이 null입니다!");
        }

        if (searchQuery == null || searchQuery.isBlank()) {
            return logsRepository.findAllByProjectId(projectId, pageable)
                    .map(LogsDto::fromEntity);
        }

        Page<Logs> searchResult = switch (searchColumn) {
            case "logUrl" -> logsRepository.findByProjectIdAndLogUrlContaining(projectId, searchQuery, pageable);
            case "method" -> logsRepository.findByProjectIdAndMethodContaining(projectId, searchQuery, pageable);
            case "ip" -> logsRepository.findByProjectIdAndIpContaining(projectId, searchQuery, pageable);
            case "username" -> logsRepository.findByProjectIdAndUsernameContaining(projectId, searchQuery, pageable);
            case "statusCode" -> logsRepository.findByProjectIdAndStatusCodeContaining(projectId, searchQuery, pageable);
            case "requestBody" -> logsRepository.findByProjectIdAndRequestBodyContaining(projectId, searchQuery, pageable);
            case "responseBody" -> logsRepository.findByProjectIdAndResponseBodyContaining(projectId, searchQuery, pageable);
            case "errorMessage" -> logsRepository.findByProjectIdAndErrorMessageContaining(projectId, searchQuery, pageable);
            default -> logsRepository.findAllByProjectId(projectId, pageable); // fallback
        };

        return searchResult.map(LogsDto::fromEntity);
    }
}
