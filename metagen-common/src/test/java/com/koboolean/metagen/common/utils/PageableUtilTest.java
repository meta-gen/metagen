package com.koboolean.metagen.common.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class PageableUtilTest {

    @Test
    @DisplayName("정렬값이 없으면 기본 컬럼(id) 내림차순으로 정렬한다")
    void defaultSort() {
        Pageable pageable = PageableUtil.getGridPageable(2, 20, null);

        assertThat(pageable.getPageNumber()).isEqualTo(2);
        assertThat(pageable.getPageSize()).isEqualTo(20);
        assertThat(pageable.getSort()).containsExactly(Sort.Order.desc("id"));
    }

    @Test
    @DisplayName("빈 정렬값은 지정한 기본 컬럼 내림차순으로 정렬한다")
    void blankSortUsesGivenDefault() {
        Pageable pageable = PageableUtil.getGridPageable(0, 10, "  ", "timestamp");

        assertThat(pageable.getSort()).containsExactly(Sort.Order.desc("timestamp"));
    }

    @Test
    @DisplayName("'컬럼,방향' 을 ; 로 구분해 여러 정렬 조건을 순서대로 적용한다")
    void multiSort() {
        Pageable pageable = PageableUtil.getGridPageable(0, 10, "name, asc;id,DESC");

        assertThat(pageable.getSort()).containsExactly(Sort.Order.asc("name"), Sort.Order.desc("id"));
    }

    @Test
    @DisplayName("방향이 없는 정렬 조건은 무시한다")
    void invalidSortEntriesIgnored() {
        Pageable pageable = PageableUtil.getGridPageable(0, 10, "name;id,asc");

        assertThat(pageable.getSort()).containsExactly(Sort.Order.asc("id"));
    }

    @Test
    @DisplayName("그리드 응답은 data / recordsTotal / recordsFiltered 를 담는다")
    void gridPageableMap() {
        PageImpl<String> page = new PageImpl<>(List.of("a", "b"), PageRequest.of(0, 2), 5);

        ResponseEntity<Map<String, Object>> response = PageableUtil.getGridPageableMap(page);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody())
                .containsEntry("data", List.of("a", "b"))
                .containsEntry("recordsTotal", 5L)
                .containsEntry("recordsFiltered", 5L);
    }
}
