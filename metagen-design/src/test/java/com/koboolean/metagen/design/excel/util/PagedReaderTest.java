package com.koboolean.metagen.design.excel.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class PagedReaderTest {

    /** total 건의 데이터를 요청한 페이지만큼 잘라 돌려주는 가짜 조회 (요청 기록) */
    private static Function<Pageable, Page<Integer>> fakeQuery(int total, List<Pageable> requests) {
        List<Integer> all = IntStream.rangeClosed(1, total).boxed().toList();
        return pageable -> {
            requests.add(pageable);
            int from = (int) Math.min(pageable.getOffset(), total);
            int to = Math.min(from + pageable.getPageSize(), total);
            return new PageImpl<>(all.subList(from, to), pageable, total);
        };
    }

    @Test
    @DisplayName("페이지 크기(500)씩 나눠 조회해 모든 건을 순서대로 넘기고, 페이지마다 afterPage 를 호출한다")
    void readsAllPages() {
        List<Pageable> requests = new ArrayList<>();
        List<Integer> received = new ArrayList<>();
        int[] afterPageCalls = {0};

        PagedReader.forEach(fakeQuery(1_201, requests), Sort.by("id"), received::add, () -> afterPageCalls[0]++);

        assertThat(received).hasSize(1_201).startsWith(1, 2, 3).endsWith(1_201);
        assertThat(requests).extracting(Pageable::getPageNumber).containsExactly(0, 1, 2);
        assertThat(requests).allSatisfy(p -> {
            assertThat(p.getPageSize()).isEqualTo(PagedReader.PAGE_SIZE);
            assertThat(p.getSort()).as("페이지 사이 순서 고정").isEqualTo(Sort.by("id"));
        });
        assertThat(afterPageCalls[0]).as("영속성 컨텍스트 비우기는 페이지마다").isEqualTo(3);
    }

    @Test
    @DisplayName("데이터가 없으면 한 번만 조회하고 끝난다")
    void empty() {
        List<Pageable> requests = new ArrayList<>();
        List<Integer> received = new ArrayList<>();

        PagedReader.forEach(fakeQuery(0, requests), Sort.by("id"), received::add, null);

        assertThat(received).isEmpty();
        assertThat(requests).hasSize(1);
    }

    @Test
    @DisplayName("정확히 한 페이지 분량이면 다음 페이지를 조회하지 않는다")
    void exactlyOnePage() {
        List<Pageable> requests = new ArrayList<>();

        PagedReader.forEach(fakeQuery(PagedReader.PAGE_SIZE, requests), Sort.by("id"), value -> { }, null);

        assertThat(requests).hasSize(1);
    }
}
