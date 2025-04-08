package com.koboolean.metagen.security.repository;

import com.koboolean.metagen.security.domain.dto.ResourcesDto;
import com.koboolean.metagen.security.domain.entity.Resources;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ResourcesRepository extends JpaRepository<Resources, Long> {
    Resources findByResourceNameAndHttpMethod(String resourceName, String httpMethod);

    @Query("select r from Resources r join fetch r.roleSet where r.resourceType = 'url' order by r.orderNum desc")
    List<Resources> findAllResources();

    Page<Resources> findAllByResourceNameLike(String resourceName, Pageable pageable);

    Page<Resources> findAllByResourceDescLike(String resourceDesc, Pageable pageable);
}
