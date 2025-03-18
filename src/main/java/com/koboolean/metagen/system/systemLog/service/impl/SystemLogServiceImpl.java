package com.koboolean.metagen.system.systemLog.service.impl;

import com.koboolean.metagen.grid.enums.ColumnType;
import com.koboolean.metagen.logs.domain.dto.LogsDto;
import com.koboolean.metagen.logs.domain.entity.Logs;
import com.koboolean.metagen.logs.repository.LogsRepository;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.system.systemLog.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SystemLogServiceImpl implements SystemLogService {

    private final LogsRepository logsRepository;

    @Override
    public List<ColumnDto> getSystemLogColumn() {
        return List.of(
                new ColumnDto("id","id")
                , new ColumnDto("요청 URL","logUrl")
                , new ColumnDto("메소드","method")
                , new ColumnDto("IP","ip")
                , new ColumnDto("사용자 ID","username")
                , new ColumnDto("권한","roleName")
                , new ColumnDto("응답 HTTP 상태코드","statusCode")
                , new ColumnDto("요청 데이터","requestBody")
                , new ColumnDto("응답 데이터","responseBody")
                , new ColumnDto("에러 메시지","errorMessage")
                , new ColumnDto("브라우저 정보","userAgent")
                , new ColumnDto("요청 발생시간","timestamp")
        );
    }

    @Override
    public Page<LogsDto> getSystemLogData(Pageable pageable, AccountDto accountDto) {

        Long projectId = accountDto.getProjectId();

        if (projectId == null) {
            throw new IllegalArgumentException("projectId 값이 null입니다!");
        }

        return logsRepository.findAllByProjectId(projectId, pageable)
                .map(log -> LogsDto.builder()
                        .id(log.getId())
                        .projectId(log.getProjectId())
                        .logUrl(log.getLogUrl())
                        .method(log.getMethod())
                        .ip(log.getIp())
                        .username(log.getUsername())
                        .roleName(log.getRoleName())
                        .statusCode(log.getStatusCode())
                        .requestBody(log.getRequestBody())
                        .responseBody(log.getResponseBody())
                        .errorMessage(log.getErrorMessage())
                        .userAgent(log.getUserAgent())
                        .timestamp(log.getTimestamp())
                        .build());
    }
}
