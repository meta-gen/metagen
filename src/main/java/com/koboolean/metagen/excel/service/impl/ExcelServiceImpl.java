package com.koboolean.metagen.excel.service.impl;

import com.koboolean.metagen.data.code.domain.dto.CodeRuleDetailDto;
import com.koboolean.metagen.data.code.domain.entity.CodeRuleDetail;
import com.koboolean.metagen.data.code.repository.CodeRuleDetailRepository;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardWordDto;
import com.koboolean.metagen.data.dictionary.repository.StandardDomainRepository;
import com.koboolean.metagen.data.dictionary.repository.StandardTermRepository;
import com.koboolean.metagen.data.dictionary.repository.StandardWordRepository;
import com.koboolean.metagen.excel.domain.dto.ExcelDto;
import com.koboolean.metagen.excel.service.ExcelService;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.utils.ExcelUtils;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {

    private final CodeRuleDetailRepository codeRuleDetailRepository;

    private final StandardWordRepository standardWordRepository;
    private final StandardTermRepository standardTermRepository;
    private final StandardDomainRepository standardDomainRepository;

    @Override
    public ResponseEntity<Resource> getExcelFile(String templateName, AccountDto accountDto) throws IOException {

        Resource resource = null;

        if(templateName.equals("dictionary")){
            Long projectId = accountDto.getProjectId();

            List<StandardWordDto> wordDtos = standardWordRepository.findAllByProjectIdAndIsApproval(projectId, true).stream().map(StandardWordDto::fromEntity).toList();
            List<StandardTermDto> termDtos = standardTermRepository.findAllByProjectIdAndIsApproval(projectId, true).stream().map(StandardTermDto::fromEntity).toList();
            List<StandardDomainDto> domainDtos = standardDomainRepository.findAllByProjectIdAndIsApproval(projectId, true).stream().map(StandardDomainDto::fromEntity).toList();

            Map dtos = Map.of("wordDtos", wordDtos, "termDtos", termDtos, "domainDtos", domainDtos);

            File file = ExcelUtils.loadAndCopyDictionaryExcelFile(templateName, dtos);
            resource = new InputStreamResource(new FileInputStream(file));
        }else{
            resource = ExcelUtils.downloadExcelFile(templateName);
        }
        String encodedFileName = java.net.URLEncoder.encode(templateName, "UTF-8").replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + ".xlsx\"")
                .body(resource);
    }

    @Override
    public ResponseEntity<Resource> getDesignExcelFile(ExcelDto excelDto, AccountDto accountDto) throws IOException {

        List<CodeRuleDetailDto> excelData = null;
        if(excelDto.getType().equals("all")){
            excelData = getExcelData(excelDto, accountDto);
        }else{
            excelData = excelDto.getRules();
        }

        List<String> data = null;

        switch(excelDto.getFormatType()){
            case "functionSpec":
                data = List.of("methodKeyword|codeRuleName", "methodName", "methodPurpose", "", "methodPurpose");
                break;
            case "interfaceSpec":
                data = List.of("methodName", "methodPurpose", "input", "output", "exception");
                break;
            case "technicalSpec":
                data = List.of("functionGroup", "methodName", "output", "methodPurpose", "", "");
                break;
            default:
                break;
        }

        File file = ExcelUtils.loadAndCopyExcelFile(excelDto.getFormatType(), excelData, data);
        String encodedFileName = java.net.URLEncoder.encode(excelDto.getFormatText(), "UTF-8").replaceAll("\\+", "%20");

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + ".xlsx\"")
                .contentLength(file.length())
                .body(resource);
    }

    @Override
    public ResponseEntity<Resource> getTestExcelFile(ExcelDto excelDto, AccountDto accountDto) throws IOException {

        List<CodeRuleDetailDto> excelData = null;
        if(excelDto.getType().equals("all")){
            excelData = getExcelData(excelDto, accountDto);
        }else{
            excelData = excelDto.getRules();
        }

        List<String> data = List.of("", "codeRuleName", "methodName", "input", "methodPurpose", "");

        File file = ExcelUtils.loadAndCopyExcelFile(excelDto.getFormatType(), excelData, data);
        String encodedFileName = java.net.URLEncoder.encode(excelDto.getFormatText(), "UTF-8").replaceAll("\\+", "%20");

        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + ".xlsx\"")
                .contentLength(file.length())
                .body(resource);
    }


    /**
     * 선택한 데이터 없을 경우 엑셀 데이터를 조회해온다.
     * @param excelDto
     * @param accountDto
     * @return
     */
    private List<CodeRuleDetailDto> getExcelData(ExcelDto excelDto, AccountDto accountDto) {
        Long projectId = accountDto.getProjectId();

        List<CodeRuleDetail> all = null;

        String searchData = "";

        if(excelDto.getSearchValue().isEmpty()){
            all = codeRuleDetailRepository.findAllByProjectId(projectId);
        }else{
            searchData = "%" + excelDto.getSearchValue() + "%";

            if(excelDto.getColumn().equals("functionGroup")){
                all = codeRuleDetailRepository.findAllByProjectIdAndFunctionGroupLike(projectId, searchData);
            }else if(excelDto.getColumn().equals("methodKeyword")){
                all = codeRuleDetailRepository.findAllByProjectIdAndMethodKeywordLike(projectId, searchData);
            }else if(excelDto.getColumn().equals("templateName")){
                all = codeRuleDetailRepository.findAllByProjectIdAndCodeRule_Template_TemplateNameLike(projectId, searchData);
            }else if(excelDto.getColumn().equals("codeRuleName")){
                all = codeRuleDetailRepository.findAllByProjectIdAndCodeRule_CodeRuleNameLike(projectId, searchData);
            }
        }

        return all.stream().map(CodeRuleDetailDto::fromEntity).collect(Collectors.toList());
    }


}
