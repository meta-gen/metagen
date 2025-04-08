package com.koboolean.metagen.security.repository;

import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.domain.entity.Account;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<Account, Long> {
    Account findByUsername(String username);

    Page<Account> findAllByIsActive(boolean b, Pageable pageable);

    Account findByUsernameAndIsActive(String username, Boolean isActive);
}
