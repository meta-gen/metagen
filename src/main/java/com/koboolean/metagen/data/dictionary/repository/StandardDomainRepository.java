package com.koboolean.metagen.data.dictionary.repository;

import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.web.bind.annotation.RestController;

@RestController
public interface StandardDomainRepository extends JpaRepository<StandardDomain, Long> {
    Page<StandardDomain> findAllByProjectId(Long projectId, Pageable pageable);
}
