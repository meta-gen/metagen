package com.koboolean.metagen.logs.repository;

import com.koboolean.metagen.logs.domain.entity.Logs;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogsRepository extends JpaRepository<Logs, Long> {

    Page<Logs> findAllByProjectId(Long projectId, Pageable pageable);

    // 동적 검색을 위한 메서드들
    Page<Logs> findByProjectIdAndLogUrlContaining(Long projectId, String logUrl, Pageable pageable);
    Page<Logs> findByProjectIdAndMethodContaining(Long projectId, String method, Pageable pageable);
    Page<Logs> findByProjectIdAndIpContaining(Long projectId, String ip, Pageable pageable);
    Page<Logs> findByProjectIdAndUsernameContaining(Long projectId, String username, Pageable pageable);
    Page<Logs> findByProjectIdAndStatusCodeContaining(Long projectId, String statusCode, Pageable pageable);
    Page<Logs> findByProjectIdAndRequestBodyContaining(Long projectId, String requestBody, Pageable pageable);
    Page<Logs> findByProjectIdAndResponseBodyContaining(Long projectId, String responseBody, Pageable pageable);
    Page<Logs> findByProjectIdAndErrorMessageContaining(Long projectId, String errorMessage, Pageable pageable);
}
