package com.koboolean.metagen.security.repository;

import com.koboolean.metagen.domain.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Account, Long> {
    Account findByUsername(String username);
}
