package com.koboolean.metagen.excel.service;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

public interface ExcelService {
    ResponseEntity<Resource> getExcelFile(String templateName) throws IOException;
}
