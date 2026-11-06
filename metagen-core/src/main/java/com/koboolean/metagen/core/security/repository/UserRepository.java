package com.koboolean.metagen.core.security.repository;

import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.security.domain.entity.Account;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Account, Long> {
    Account findByUsername(String username);

    Page<Account> findAllByIsActive(boolean b, Pageable pageable);

    Account findByUsernameAndIsActive(String username, Boolean isActive);
}
