package com.koboolean.metagen.utils;

import com.koboolean.metagen.logs.domain.dto.LogsDto;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.data.domain.Sort.by;

public class PageableUtil {

    /**
     * 페이징 처리를 위한 Pageable을 생성한다.
     * @param page
     * @param size
     * @param sort
     * @return
     */
    public static Pageable getGridPageable(int page, int size, String sort) {
        List<Sort.Order> orderList = Arrays.stream(sort.split(";")) // 여러 정렬 조건 지원
                .map(s -> s.split(",")) // 쉼표(`,`) 기준으로 나누기
                .filter(arr -> arr.length > 1) // 잘못된 데이터 필터링
                .map(arr -> {
                    String field = arr[0].trim();
                    String direction = arr[1].trim();
                    return new Sort.Order(Sort.Direction.fromString(direction), field);
                })
                .toList();

        return PageRequest.of(page, size, by(orderList));
    }

    /**
     * 페이지를위한 ResponseEntity를 생성한다.
     * @param logsPage
     * @return
     */
    public static <T> ResponseEntity<Map<String, Object>> getGridPageableMap(Page<T> logsPage) {
        Map<String, Object> response = new HashMap<>();
        response.put("data", logsPage.getContent()); // 현재 페이지 데이터
        response.put("recordsTotal", logsPage.getTotalElements()); // 전체 데이터 개수
        response.put("recordsFiltered", logsPage.getTotalElements()); // 필터링된 데이터 개수
        return ResponseEntity.ok(response);
    }
}
