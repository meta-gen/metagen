package com.koboolean.metagen.utils;

import com.koboolean.metagen.logs.domain.dto.LogsDto;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.springframework.data.domain.Sort.by;

public class PageableUtil {

    /**
     * 페이징 처리를 위한 Pageable을 생성한다.
     * @param page
     * @param size
     * @param sort
     * @return
     */
    public static Pageable getGridPageable(int page, int size, String sort, String order) {
        List<Sort.Order> orderList = Optional.ofNullable(sort)
                .filter(s -> !s.isBlank()) // 빈 문자열 방지
                .map(s -> Arrays.stream(s.split(";")) // 여러 정렬 조건 지원
                        .map(arr -> arr.split(","))
                        .filter(arr -> arr.length > 1) // 올바른 데이터만 변환
                        .map(arr -> new Sort.Order(Sort.Direction.fromString(arr[1].trim()), arr[0].trim()))
                        .toList())
                .orElse(List.of(new Sort.Order(Sort.Direction.DESC, order))); // 기본 정렬 추가

        return PageRequest.of(page, size, by(orderList));
    }

    /**
     * 페이징 처리를 위한 Pageable을 생성한다.
     * Pageable의 default를 "id"로 지정한다.
     * @param page
     * @param size
     * @param sort
     * @return
     */
    public static Pageable getGridPageable(int page, int size, String sort) {
        return getGridPageable(page, size, sort, "id");
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
