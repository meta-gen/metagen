package com.koboolean.metagen.excel.service;

import com.koboolean.metagen.excel.domain.dto.ExcelDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

public interface ExcelService {
    ResponseEntity<Resource> getExcelFile(String templateName) throws IOException;

    ResponseEntity<Resource> getDesignExcelFile(ExcelDto excelDto, AccountDto accountDto) throws IOException;

    ResponseEntity<Resource> getTestExcelFile(ExcelDto excelDto, AccountDto accountDto) throws IOException;
}
