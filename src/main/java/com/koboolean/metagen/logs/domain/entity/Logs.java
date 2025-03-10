package com.koboolean.metagen.logs.domain.entity;

import com.koboolean.metagen.home.jpa.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Logs {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "log_url", nullable = false)
    private String logUrl; // 요청 URL

    @Column(nullable = false)
    private String method; // GET, POST 등

    @Column(nullable = false)
    private String ip; // 요청한 클라이언트 IP

    @Column(name = "username")
    private String username; // 사용자 아이디 (로그인 사용자)

    @Column(name = "role_name")
    private String roleName; // 권한명

    @Column(name = "status_code")
    private Integer statusCode; // 응답 HTTP 상태 코드

    @Column(columnDefinition = "TEXT")
    private String requestBody; // 요청 데이터 (JSON 형태로 저장 가능)

    @Column(columnDefinition = "TEXT")
    private String responseBody; // 응답 데이터 (JSON 형태로 저장 가능)

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage; // 예외 발생 시 에러 메시지

    @Column(name = "user_agent")
    private String userAgent; // 브라우저, OS 정보

    @Column(nullable = false)
    private LocalDateTime timestamp; // 요청 발생 시간

    @PrePersist
    public void setTimestamp() {
        this.timestamp = LocalDateTime.now();
    }

}
