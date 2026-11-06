package com.koboolean.metagen.core.security.repository;

import com.koboolean.metagen.core.security.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByRoleName(String roleNotApprove);
}
