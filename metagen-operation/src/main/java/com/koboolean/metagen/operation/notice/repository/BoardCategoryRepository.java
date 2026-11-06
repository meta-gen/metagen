package com.koboolean.metagen.operation.notice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.koboolean.metagen.operation.board.domain.entity.BoardCategory;

@Repository
public interface BoardCategoryRepository extends JpaRepository<BoardCategory, String> {
}