package com.koboolean.metagen.operation.notice.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.koboolean.metagen.board.domain.entity.Board;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    Page<Board> findAllByProjectId(Long projectId, Pageable pageable);

    Page<Board> findAllByProjectIdAndTitleLike(Long projectId, String searchQuery, Pageable pageable);
}
