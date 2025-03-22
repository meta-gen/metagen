package com.koboolean.metagen.excel.service.impl;

import com.koboolean.metagen.excel.service.ExcelService;
import com.koboolean.metagen.utils.ExcelUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

@Service
public class ExcelServiceImpl implements ExcelService {


    @Override
    public ResponseEntity<Resource> getExcelFile(String templateName) throws IOException {
        Resource resource = ExcelUtils.downloadExcelFile(templateName);
        String encodedFileName = java.net.URLEncoder.encode(templateName, "UTF-8").replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + ".xlsx\"")
                .body(resource);
    }
}
