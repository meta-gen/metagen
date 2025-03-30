package com.koboolean.metagen.logs.domain.dto;

import com.koboolean.metagen.logs.domain.entity.Logs;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogsDto {

    private Long id;
    private Long projectId;
    private String logUrl; // 요청 URL
    private String method; // GET, POST 등
    private String ip; // 요청한 클라이언트 IP
    private String username; // 사용자 아이디 (로그인 사용자)
    private String roleName; // 권한명
    private Integer statusCode; // 응답 HTTP 상태 코드
    private String requestBody; // 요청 데이터 (JSON 형태로 저장 가능)
    private String responseBody; // 응답 데이터 (JSON 형태로 저장 가능)
    private String errorMessage; // 예외 발생 시 에러 메시지
    private String userAgent; // 브라우저, OS 정보
    private LocalDateTime timestamp; // 요청 발생 시간

    public static LogsDto fromEntity(Logs log) {
        return LogsDto.builder()
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
                .build();
    }
}
