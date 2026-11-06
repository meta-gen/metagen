package com.koboolean.metagen.core.logs.service.impl;

import com.koboolean.metagen.core.logs.domain.entity.Logs;
import com.koboolean.metagen.core.logs.repository.LogsRepository;
import com.koboolean.metagen.core.logs.service.LogsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogsServiceImpl implements LogsService {

    private final LogsRepository logsRepository;

    @Override
    @Transactional
    public void saveLogs(Logs log) {
        logsRepository.save(log);
    }
}
