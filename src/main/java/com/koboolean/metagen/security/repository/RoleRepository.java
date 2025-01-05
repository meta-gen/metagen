package com.koboolean.metagen.security.repository;

import com.koboolean.metagen.domain.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Role findByRoleName(String roleNotApprove);
}
