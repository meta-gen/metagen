package com.koboolean.metagen.system.systemLog.controller;

import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.logs.domain.dto.LogsDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.system.systemLog.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.domain.Sort.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SystemLogRestController {

    private final SystemLogService systemLogService;

    @GetMapping("/getSystemLog/column")
    public ResponseEntity<List<ColumnDto>> getSystemLogColumn() {
        return ResponseEntity.ok(systemLogService.getSystemLogColumn());
    }

    @GetMapping("/getSystemLog/data")
    public ResponseEntity<Map<String,Object>> getSystemLogData(@AuthenticationPrincipal AccountDto accountDto,
                                                               @RequestParam int page,
                                                               @RequestParam int size,
                                                               @RequestParam(required = false) String sort) {
        List<Sort.Order> orderList = Arrays.stream(sort.split(";")) // 여러 정렬 조건 지원
                .map(s -> s.split(",")) // 쉼표(`,`) 기준으로 나누기
                .filter(arr -> arr.length > 1) // 잘못된 데이터 필터링
                .map(arr -> {
                    String field = arr[0].trim();
                    String direction = arr[1].trim();
                    return new Sort.Order(Sort.Direction.fromString(direction), field);
                })
                .toList();

        Pageable pageable = PageRequest.of(page, size, by(orderList));

        Page<LogsDto> logsPage = systemLogService.getSystemLogData(pageable, accountDto);


        Map<String, Object> response = new HashMap<>();
        response.put("data", logsPage.getContent()); // 현재 페이지 데이터
        response.put("recordsTotal", logsPage.getTotalElements()); // 전체 데이터 개수
        response.put("recordsFiltered", logsPage.getTotalElements()); // 필터링된 데이터 개수

        return ResponseEntity.ok(response);
    }
}
