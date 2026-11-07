package com.koboolean.metagen.design.excel.service.impl;

import com.koboolean.metagen.design.excel.util.ExcelExportUtils;
import com.koboolean.metagen.design.excel.util.ExcelExportUtils.RowSource;
import com.koboolean.metagen.design.excel.util.PagedReader;

import com.koboolean.metagen.design.code.domain.dto.CodeRuleDetailDto;
import com.koboolean.metagen.design.code.domain.entity.CodeRuleDetail;
import com.koboolean.metagen.design.code.repository.CodeRuleDetailRepository;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardWordDto;
import com.koboolean.metagen.data.dictionary.repository.StandardDomainRepository;
import com.koboolean.metagen.data.dictionary.repository.StandardTermRepository;
import com.koboolean.metagen.data.dictionary.repository.StandardWordRepository;
import com.koboolean.metagen.design.excel.domain.dto.ExcelDto;
import com.koboolean.metagen.design.excel.service.ExcelService;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.common.utils.ExcelUtils;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class ExcelServiceImpl implements ExcelService {

    private final CodeRuleDetailRepository codeRuleDetailRepository;

    private final StandardWordRepository standardWordRepository;
    private final StandardTermRepository standardTermRepository;
    private final StandardDomainRepository standardDomainRepository;

    /** 페이지 단위 조회 후 영속성 컨텍스트를 비우기 위해 사용 */
    private final EntityManager entityManager;

    /** 페이지 사이 순서가 바뀌지 않도록 PK 기준으로 정렬한다 */
    private static final Sort PAGE_SORT = Sort.by("id");

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getExcelFile(String templateName, AccountDto accountDto) throws IOException {

        Resource resource = null;

        if(templateName.equals("dictionary")){
            Long projectId = accountDto.getProjectId();

            // 승인된 데이터를 페이지 단위로 조회하며 바로 엑셀에 쓴다. (목록 전체를 메모리에 올리지 않는다)
            RowSource<StandardTermDto> terms = paged(
                    pageable -> standardTermRepository.findByIsApprovalAndProjectId(true, projectId, pageable), StandardTermDto::fromEntity);
            RowSource<StandardWordDto> words = paged(
                    pageable -> standardWordRepository.findByIsApprovalAndProjectId(true, projectId, pageable), StandardWordDto::fromEntity);
            RowSource<StandardDomainDto> domains = paged(
                    pageable -> standardDomainRepository.findByIsApprovalAndProjectId(true, projectId, pageable), StandardDomainDto::fromEntity);

            File file = ExcelExportUtils.loadAndCopyDictionaryExcelFile(templateName, terms, words, domains);
            resource = new InputStreamResource(openAndDeleteOnClose(file));
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
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getDesignExcelFile(ExcelDto excelDto, AccountDto accountDto) throws IOException {

        RowSource<CodeRuleDetailDto> excelData = getExcelRows(excelDto, accountDto);

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

        File file = ExcelExportUtils.loadAndCopyExcelFile(excelDto.getFormatType(), excelData, data);
        String encodedFileName = java.net.URLEncoder.encode(excelDto.getFormatText(), "UTF-8").replaceAll("\\+", "%20");

        InputStreamResource resource = new InputStreamResource(openAndDeleteOnClose(file));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + ".xlsx\"")
                .contentLength(file.length())
                .body(resource);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Resource> getTestExcelFile(ExcelDto excelDto, AccountDto accountDto) throws IOException {

        RowSource<CodeRuleDetailDto> excelData = getExcelRows(excelDto, accountDto);

        List<String> data = List.of("", "codeRuleName", "methodName", "input", "methodPurpose", "");

        File file = ExcelExportUtils.loadAndCopyExcelFile(excelDto.getFormatType(), excelData, data);
        String encodedFileName = java.net.URLEncoder.encode(excelDto.getFormatText(), "UTF-8").replaceAll("\\+", "%20");

        InputStreamResource resource = new InputStreamResource(openAndDeleteOnClose(file));

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + encodedFileName + ".xlsx\"")
                .contentLength(file.length())
                .body(resource);
    }


    /**
     * 출력할 코드규칙 : 전체(all)면 검색 조건으로 페이지 단위 조회, 아니면 화면에서 선택한 목록
     */
    private RowSource<CodeRuleDetailDto> getExcelRows(ExcelDto excelDto, AccountDto accountDto) {
        if(!excelDto.getType().equals("all")){
            List<CodeRuleDetailDto> rules = excelDto.getRules() == null ? List.of() : excelDto.getRules();
            return rules::forEach;
        }

        Long projectId = accountDto.getProjectId();
        String searchValue = excelDto.getSearchValue() == null ? "" : excelDto.getSearchValue();

        if(searchValue.isEmpty()){
            return paged(pageable -> codeRuleDetailRepository.findByProjectId(projectId, pageable), CodeRuleDetailDto::fromEntity);
        }

        String searchData = "%" + searchValue + "%";

        Function<Pageable, Page<CodeRuleDetail>> query = switch (excelDto.getColumn()) {
            case "functionGroup" -> pageable -> codeRuleDetailRepository.findAllByProjectIdAndFunctionGroupLike(projectId, searchData, pageable);
            case "methodKeyword" -> pageable -> codeRuleDetailRepository.findAllByProjectIdAndMethodKeywordLike(projectId, searchData, pageable);
            case "templateName" -> pageable -> codeRuleDetailRepository.findAllByProjectIdAndCodeRule_Template_TemplateNameLike(projectId, searchData, pageable);
            case "codeRuleName" -> pageable -> codeRuleDetailRepository.findAllByProjectIdAndCodeRule_CodeRuleNameLike(projectId, searchData, pageable);
            default -> null;
        };

        // 알 수 없는 검색 컬럼이면 출력할 데이터가 없다.
        if(query == null) return action -> { };

        return paged(query, CodeRuleDetailDto::fromEntity);
    }

    /**
     * 페이지 단위로 조회해 DTO 로 바꿔 넘기는 소스. 한 페이지를 쓴 뒤 영속성 컨텍스트를 비운다.
     * (DTO 변환에 지연 로딩이 필요하므로 페이지를 다 쓴 다음에 비운다)
     */
    private <E, D> RowSource<D> paged(Function<Pageable, ? extends Page<E>> query, Function<E, D> toDto) {
        return action -> PagedReader.forEach(query, PAGE_SORT, entity -> action.accept(toDto.apply(entity)), entityManager::clear);
    }

    /**
     * 출력용 임시 파일을 연다. 다운로드가 끝나 스트림이 닫히면 임시 파일이 삭제된다.
     */
    private static InputStream openAndDeleteOnClose(File file) throws IOException {
        return Files.newInputStream(file.toPath(), StandardOpenOption.DELETE_ON_CLOSE);
    }
}
