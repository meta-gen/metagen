package com.koboolean.metagen.system.access.controller;

import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.domain.dto.ResourcesDto;
import com.koboolean.metagen.system.access.service.AccessService;
import com.koboolean.metagen.system.project.domain.dto.ProjectMemberDto;
import com.koboolean.metagen.utils.PageableUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Access Rest API", description = "인가관리를 위한 API")
public class AccessRestController {

    private final AccessService accessService;

    @Operation(summary = "인가관리 컬럼 조회", description = "인가관리 테이블의 컬럼 목록을 조회합니다.")
    @GetMapping("/selectAccess/column")
    public ResponseEntity<List<ColumnDto>> getAccessColumn() {
        return ResponseEntity.ok(accessService.getAccessColumn());
    }

    @Operation(summary = "인가관리 데이터 조회", description = "인가관리 데이터를 조회합니다.")
    @GetMapping("/selectAccess/data")
    public ResponseEntity<Map<String,Object>> getAccessData(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(value = "page") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(value = "size") int size,
            @Parameter(description = "정렬 조건 (예: timestamp,desc;id,asc)", example = "id,desc")
            @RequestParam(required = false, value = "sort") String sort,
            @Parameter(description = "조회 용어명", example = "용어명1")
            @RequestParam(required = false, value = "searchQuery") String searchQuery,
            @Parameter(description = "조회컬럼 명", example = "id")
            @RequestParam(required = false, value = "searchColumn") String searchColumn,
            @Parameter(description = "현재 로그인된 사용자 정보", hidden = true)
            @AuthenticationPrincipal AccountDto accountDto
    ) {
        Pageable pageable = PageableUtil.getGridPageable(page, size, sort);
        Page<ResourcesDto> standardTermDataPage = accessService.getAccessData(pageable, searchColumn, searchQuery, accountDto);
        return PageableUtil.getGridPageableMap(standardTermDataPage);
    }

    @Operation(summary = "인가관리 권한 변경", description = "인가관리 권한을 변경합니다.")
    @PutMapping("/updateAccess/role")
    public ResponseEntity<Map<String,Boolean>> updateAccessRole(@RequestBody List<ResourcesDto> resourcesDtos) {
        accessService.updateAccessRole(resourcesDtos);

        return ResponseEntity.ok(Map.of("result", true));
    }

}
