package com.koboolean.metagen.operation.notice.repository;

import groovyjarjarasm.asm.commons.Remapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.koboolean.metagen.board.domain.entity.Board;

import java.util.Optional;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {

    Page<Board> findAllByProjectId(Long projectId, Pageable pageable);

    Page<Board> findAllByProjectIdAndTitleLike(Long projectId, String searchQuery, Pageable pageable);

    Optional<Board> findByIdAndProjectId(Long id, Long projectId);

    Page<Board> findAllByProjectIdAndDeleteYn(Long selectedId, char deleteYn, Pageable pageable);
}
