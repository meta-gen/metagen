package com.koboolean.metagen.operationManagement.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.koboolean.metagen.board.domain.entity.Board;

public interface NoticeRepository extends JpaRepository<Board, Integer> {
}