package com.koboolean.metagen.design.excel.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * 대량 데이터를 한 번에 목록으로 가져오지 않고 페이지 단위로 조회해 한 건씩 넘긴다. (엑셀 출력용)
 *
 * 영속성 컨텍스트에는 조회한 엔티티가 계속 쌓이므로(open-in-view / 트랜잭션), 페이지를 처리한 뒤
 * afterPage 로 EntityManager.clear() 를 넘겨 비워야 실제로 메모리가 줄어든다.
 */
public final class PagedReader {

    /** 한 번에 조회할 건수 */
    public static final int PAGE_SIZE = 500;

    private PagedReader() {
    }

    /**
     * @param query     페이지 조회 (예: pageable -> repository.findByProjectId(projectId, pageable))
     * @param sort      페이지 사이 순서가 바뀌지 않도록 고정할 정렬 (예: Sort.by("id"))
     * @param action    한 건씩 처리
     * @param afterPage 한 페이지를 처리한 뒤 호출 (예: entityManager::clear)
     */
    public static <E> void forEach(Function<Pageable, ? extends Slice<E>> query, Sort sort,
                                   Consumer<? super E> action, Runnable afterPage) {
        Pageable pageable = PageRequest.of(0, PAGE_SIZE, sort);

        while (true) {
            Slice<E> slice = query.apply(pageable);
            slice.forEach(action);

            boolean hasNext = slice.hasNext();
            if (afterPage != null) afterPage.run();
            if (!hasNext) return;

            pageable = pageable.next();
        }
    }
}
