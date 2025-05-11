package com.koboolean.metagen.excel.service;

import com.koboolean.metagen.excel.domain.dto.ExcelDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

import java.io.IOException;

public interface ExcelService {
    ResponseEntity<Resource> getExcelFile(String templateName, AccountDto accountDto) throws IOException;

    ResponseEntity<Resource> getDesignExcelFile(ExcelDto excelDto, AccountDto accountDto) throws IOException;

    ResponseEntity<Resource> getTestExcelFile(ExcelDto excelDto, AccountDto accountDto) throws IOException;
}
