package com.koboolean.metagen.operationManagement.notice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;

import com.koboolean.metagen.board.domain.entity.Board;

@RestController
public interface NoticeRepository extends JpaRepository<Board, Long> {

    Page<Board> findAllByProjectId(Long projectId, Pageable pageable);
}