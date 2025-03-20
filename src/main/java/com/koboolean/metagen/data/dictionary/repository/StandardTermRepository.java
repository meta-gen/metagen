package com.koboolean.metagen.data.dictionary.repository;

import com.koboolean.metagen.data.dictionary.domain.entity.StandardTerm;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;

@RestController
public interface StandardTermRepository extends JpaRepository<StandardTerm, Long> {
    Page<StandardTerm> findAllByProjectId(Long projectId, Pageable pageable);
}
