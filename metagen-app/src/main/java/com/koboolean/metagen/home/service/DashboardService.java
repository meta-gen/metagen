package com.koboolean.metagen.home.service;

import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.home.domain.dto.DashboardDto;

public interface DashboardService {

    DashboardDto selectDashboardData(AccountDto accountDto);
}
