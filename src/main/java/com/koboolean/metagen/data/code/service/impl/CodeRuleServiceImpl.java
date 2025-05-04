package com.koboolean.metagen.data.code.service.impl;

import com.koboolean.metagen.data.code.domain.dto.CodeRuleDetailDto;
import com.koboolean.metagen.data.code.domain.entity.CodeRuleDetail;
import com.koboolean.metagen.data.code.repository.CodeRuleDetailRepository;
import com.koboolean.metagen.data.code.service.CodeRuleService;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardWord;
import com.koboolean.metagen.data.dictionary.repository.StandardWordRepository;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.grid.enums.ColumnType;
import com.koboolean.metagen.grid.enums.RowType;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.exception.CustomException;
import com.koboolean.metagen.security.exception.domain.ErrorCode;
import com.koboolean.metagen.system.code.domain.dto.CodeRuleDto;
import com.koboolean.metagen.system.code.domain.entity.CodeRule;
import com.koboolean.metagen.system.code.repository.CodeRuleRepository;
import com.koboolean.metagen.system.code.repository.TemplateRepository;
import com.koboolean.metagen.system.project.domain.dto.ProjectDto;
import com.koboolean.metagen.system.project.domain.dto.TemplateDto;
import com.koboolean.metagen.system.project.domain.entity.Project;
import com.koboolean.metagen.system.project.domain.entity.Template;
import com.koboolean.metagen.system.project.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CodeRuleServiceImpl implements CodeRuleService {

    private final CodeRuleDetailRepository codeRuleDetailRepository;
    private final CodeRuleRepository codeRuleRepository;
    private final TemplateRepository templateRepository;
    private final ProjectRepository projectRepository;
    private final StandardWordRepository standardWordRepository;

    @Override
    public List<ColumnDto> selectCodeRuleColumn() {
        return List.of(
                new ColumnDto("", "id", ColumnType.NUMBER, RowType.CHECKBOX),
                new ColumnDto("그룹명", "functionGroup", ColumnType.STRING, RowType.TEXT, true, true),
                new ColumnDto("키워드", "methodKeyword", ColumnType.STRING, RowType.TEXT, true, true),
                new ColumnDto("템플릿 명", "templateName", ColumnType.STRING, RowType.TEXT, true,false),
                new ColumnDto("코드규칙 명", "codeRuleName", ColumnType.STRING, RowType.TEXT, true,false),
                new ColumnDto("기능/목적 설명", "methodPurpose", ColumnType.STRING, RowType.TEXT, false,false),
                new ColumnDto("변환된 메소드 명", "methodName", ColumnType.STRING, RowType.TEXT, false,false),
                new ColumnDto("입력값", "input", ColumnType.STRING, RowType.TEXT, false,false),
                new ColumnDto("출력값", "output", ColumnType.STRING, RowType.TEXT, false,false),
                new ColumnDto("기타 설명", "description", ColumnType.STRING, RowType.TEXT, false,false)
        );
    }

    @Override
    public Page<CodeRuleDetailDto> selectCodeRuleData(Pageable pageable, AccountDto accountDto, String searchColumn, String searchQuery, Long tableId) {
        Long projectId = accountDto.getProjectId();

        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            // 검색어가 없을 경우 전체 조회
            return codeRuleDetailRepository.findByProjectId(projectId, pageable).map(CodeRuleDetailDto::fromEntity);
        }

        if(!searchQuery.isEmpty()) {
            searchQuery = "%" + searchQuery + "%";
        }

        Page<CodeRuleDetail> columnInfos = switch (searchColumn) {
            case "functionGroup" ->
                    codeRuleDetailRepository.findAllByProjectIdAndFunctionGroupLike(projectId, searchQuery, pageable);
            case "methodKeyword" ->
                    codeRuleDetailRepository.findAllByProjectIdAndMethodKeywordLike(projectId, searchQuery, pageable);
            case "templateName" ->
                    codeRuleDetailRepository.findAllByProjectIdAndCodeRule_Template_TemplateNameLike(projectId, searchQuery, pageable);
            case "codeRuleName" ->
                    codeRuleDetailRepository.findAllByProjectIdAndCodeRule_CodeRuleNameLike(projectId, searchQuery, pageable);
            default -> null;
        };

        return columnInfos.map(CodeRuleDetailDto::fromEntity);
    }

    @Override
    public List<CodeRuleDto> selectCodeRuleDetailById(AccountDto accountDto, Long codeRuleId) {
        Long projectId = accountDto.getProjectId();

        return codeRuleRepository.findAllByProjectIdAndTemplate_Id(projectId, codeRuleId).stream().map(CodeRuleDto::fromEntity).toList();
    }

    @Override
    public CodeRuleDetailDto selectCodeRuleDetail(AccountDto accountDto, CodeRuleDetailDto codeRuleDetailDto) {

        Long projectId = accountDto.getProjectId();

        Project project = projectRepository.findById(projectId).orElse(null);
        CodeRule codeRule = codeRuleRepository.findByIdAndProjectId(codeRuleDetailDto.getCodeRuleId(), projectId);

        if(project == null || codeRule == null){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        selectCodeRuleDetail(accountDto, codeRuleDetailDto, codeRule, projectId, project);

        return codeRuleDetailDto;
    }

    @Override
    @Transactional
    public void insertCodeRule(AccountDto accountDto, CodeRuleDetailDto codeRuleDetailDto) {
        Long projectId = accountDto.getProjectId();

        CodeRule codeRule = codeRuleRepository.findByIdAndProjectId(codeRuleDetailDto.getCodeRuleId(), projectId);
        Template template = templateRepository.findByIdAndProjectId(codeRuleDetailDto.getTemplateId(), projectId);

        if(codeRule == null || template == null){
            throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }

        CodeRuleDetail codeRuleIsDupl = codeRuleDetailRepository.findByProjectIdAndFunctionGroupAndMethodKeywordAndCodeRule_Id(projectId, codeRuleDetailDto.getFunctionGroup(), codeRuleDetailDto.getMethodKeyword(), codeRuleDetailDto.getCodeRuleId());

        if(codeRuleIsDupl != null){
            throw new CustomException(ErrorCode.SAVED_DATA_EXISTS);
        }

        CodeRuleDetail codeRuleDetail = CodeRuleDetail.builder()
                .projectId(projectId)
                .functionGroup(codeRuleDetailDto.getFunctionGroup())
                .description(codeRuleDetailDto.getDescription())
                .methodKeyword(codeRuleDetailDto.getMethodKeyword())
                .methodPurpose(codeRuleDetailDto.getMethodPurpose())
                .methodName(codeRuleDetailDto.getMethodName())
                .swaggerData(codeRuleDetailDto.getSwaggerData())
                .isDicAbbrUsed(codeRuleDetailDto.getIsDicAbbrUsed())
                .useSwagger(codeRuleDetailDto.getUseSwagger())
                .input(codeRuleDetailDto.getInput())
                .output(codeRuleDetailDto.getOutput())
                .build();

        codeRuleDetail.setCodeRule(codeRule);

        codeRuleDetailRepository.save(codeRuleDetail);

        codeRule.getCodeRuleDetails().add(codeRuleDetail);
    }

    @Override
    @Transactional
    public void updateCodeRule(AccountDto accountDto, CodeRuleDetailDto codeRuleDetailDto) {
        Long projectId = accountDto.getProjectId();

        CodeRuleDetail codeRuleIsDupl = codeRuleDetailRepository.findByProjectIdAndFunctionGroupAndMethodKeywordAndCodeRule_Id(projectId, codeRuleDetailDto.getFunctionGroup(), codeRuleDetailDto.getMethodKeyword(), codeRuleDetailDto.getCodeRuleId());

        if(codeRuleIsDupl != null && !codeRuleIsDupl.getId().equals(codeRuleDetailDto.getId())){
            throw new CustomException(ErrorCode.SAVED_DATA_EXISTS);
        }

        CodeRuleDetail codeRuleDetail = codeRuleDetailRepository.findByIdAndProjectId(codeRuleDetailDto.getId(), projectId);
        CodeRule codeRule = codeRuleRepository.findById(codeRuleDetailDto.getCodeRuleId()).orElse(null);

        CodeRule removeCodeRule = codeRuleRepository.findByIdAndProjectId(codeRuleDetail.getCodeRule().getId(), projectId);
        removeCodeRule.getCodeRuleDetails().remove(codeRuleDetail);

        codeRuleDetail.setFunctionGroup(codeRuleDetailDto.getFunctionGroup());
        codeRuleDetail.setDescription(codeRuleDetailDto.getDescription());
        codeRuleDetail.setMethodKeyword(codeRuleDetailDto.getMethodKeyword());
        codeRuleDetail.setMethodPurpose(codeRuleDetailDto.getMethodPurpose());
        codeRuleDetail.setMethodName(codeRuleDetailDto.getMethodName());
        codeRuleDetail.setSwaggerData(codeRuleDetailDto.getSwaggerData());
        codeRuleDetail.setIsDicAbbrUsed(codeRuleDetailDto.getIsDicAbbrUsed());
        codeRuleDetail.setUseSwagger(codeRuleDetailDto.getUseSwagger());
        codeRuleDetail.setInput(codeRuleDetailDto.getInput());
        codeRuleDetail.setOutput(codeRuleDetailDto.getOutput());
        codeRuleDetail.setCodeRule(codeRule);

        if(codeRule != null){
            codeRule.getCodeRuleDetails().add(codeRuleDetail);
        }
    }

    @Override
    public CodeRuleDetailDto selectCodeRuleDetailDataById(AccountDto accountDto, Long id) {

        Long projectId = accountDto.getProjectId();

        CodeRuleDetail codeRuleDetail = codeRuleDetailRepository.findByIdAndProjectId(id, projectId);

        if(codeRuleDetail == null){
            throw new CustomException(ErrorCode.TABLE_IS_NOT_DEFINED);
        }

        CodeRuleDetailDto codeRuleDetailDto = CodeRuleDetailDto.fromEntity(codeRuleDetail);

        CodeRule codeRule = codeRuleRepository.findById(codeRuleDetail.getCodeRule().getId()).orElse(null);
        Project project = projectRepository.findById(projectId).orElse(null);

        selectCodeRuleDetail(accountDto, codeRuleDetailDto, Objects.requireNonNull(codeRule), projectId, project);

        return codeRuleDetailDto;
    }

    @Override
    @Transactional
    public void deleteCodeRule(AccountDto accountDto, List<CodeRuleDetailDto> dtos) {
        dtos.forEach(dto -> {
            codeRuleDetailRepository.deleteById(dto.getId());
        });
    }

    private void selectCodeRuleDetail(AccountDto accountDto, CodeRuleDetailDto codeRuleDetailDto, CodeRule codeRule, Long projectId, Project project) {
        String[] methodKeyword = codeRuleDetailDto.getMethodKeyword().split(" ");

        StringBuilder data = new StringBuilder();

        if(!codeRule.getPrefix().isEmpty()) {
            data.append(codeRule.getPrefix().toLowerCase()).append("_");
        }

        for (String s : methodKeyword) {
            List<StandardWord> standardWord = standardWordRepository.findAllByProjectIdAndCommonStandardWordName(projectId, s);

            // 명칭 키워드를 이용하여 메소드 명을 가져온다.
            if (!standardWord.isEmpty()) {
                if (project.getIsDicAbbrUsed() != null && project.getIsDicAbbrUsed()) {
                    // 약어 사용여부가 ture일 경우 약어 형식으로 사용한다.
                    data.append(standardWord.get(0).getCommonStandardWordAbbreviation().toLowerCase()).append("_");
                } else {
                    // 약어사용여부가 false이거나 null일 경우, 단어를 가져와 메소드명에 사용한다.
                    if(standardWord.get(0).getUseAbbreviation()){
                        // 약어 사용여부는 false이지만, 문장 내부에 약어사용여부가 true일 경우, 약어 형식을 사용토록 한다.
                        // 만약 IP일 경우 Internet Protocol 이므로 문장형식에 의해 너무 긴 메소드명이 만들어지므로 이를 방지하기 위해 사용한다.
                        data.append(standardWord.get(0).getCommonStandardWordAbbreviation().toLowerCase()).append("_");
                    }else{
                        data.append(standardWord.get(0).getCommonStandardWordEnglishName().replace(" ", "_").toLowerCase()).append("_");
                    }
                }
            }
        }

        codeRuleDetailDto.setIsDicAbbrUsed(project.getIsDicAbbrUsed() != null && project.getIsDicAbbrUsed());

        data.append(codeRule.getSuffix().toLowerCase());

        codeRuleDetailDto.setMethodName(toCamelCase(data.toString()));

        if(codeRuleDetailDto.getMethodPurpose() == null || codeRuleDetailDto.getMethodPurpose().isEmpty()){
            String codeDescription = "";

            if(!codeRule.getCodeRuleDescription().contains("${1}")){
                codeDescription = codeRuleDetailDto.getMethodKeyword() + codeRule.getCodeRuleDescription();
            }else{
                codeDescription = codeRule.getCodeRuleDescription().replace("${1}", codeRuleDetailDto.getMethodKeyword());
            }
            codeRuleDetailDto.setMethodPurpose(codeDescription);
        }

        String methodForm = codeRule.getMethodForm();

        String swaggerData = "@Operation(summary = \"" + codeRuleDetailDto.getMethodKeyword() + " " + codeRule.getCodeRuleName() + "\", description = \"" + codeRuleDetailDto.getMethodPurpose() + "\")\n";
        codeRuleDetailDto.setSwaggerData(swaggerData);

        // 스웨거 관련
        if(codeRuleDetailDto.getUseSwagger()){
            methodForm = swaggerData + methodForm;
        }

        methodForm = methodForm.replace("${0}", codeRuleDetailDto.getMethodName());
        methodForm = methodForm.replace("${1}", codeRuleDetailDto.getMethodKeyword());
        methodForm = methodForm.replace("${2}", codeRuleDetailDto.getMethodPurpose());
        methodForm = methodForm.replaceAll("(?i)\\$\\{USER_NAME}", accountDto.getName());
        methodForm = methodForm.replaceAll("(?i)\\$\\{INPUT}", codeRuleDetailDto.getInput() == null ? "" : codeRuleDetailDto.getInput());
        methodForm = methodForm.replaceAll("(?i)\\$\\{OUTPUT}", codeRuleDetailDto.getOutput() == null ? "" : codeRuleDetailDto.getOutput());

        String description = codeRuleDetailDto.getDescription();
        if(description != null){
            methodForm = methodForm.replace("${3}", codeRuleDetailDto.getDescription());
        }

        codeRuleDetailDto.setMethodForm(methodForm);
    }

    private String toCamelCase(String input) {
        if (input == null || input.isEmpty()) return input;

        // 끝에 _ 붙은 경우 제거
        if (input.endsWith("_")) {
            input = input.substring(0, input.length() - 1);
        }

        StringBuilder result = new StringBuilder();
        boolean toUpper = false;

        for (char ch : input.toCharArray()) {
            if (ch == '_') {
                toUpper = true;
            } else {
                if (toUpper) {
                    result.append(Character.toUpperCase(ch));
                    toUpper = false;
                } else {
                    result.append(Character.toLowerCase(ch));
                }
            }
        }

        return result.toString();
    }


}
