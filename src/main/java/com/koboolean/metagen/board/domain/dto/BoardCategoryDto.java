package com.koboolean.metagen.board.domain.dto;

import groovy.transform.builder.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(chain = true)
@AllArgsConstructor
public class BoardCategoryDto {

    /* 게시판 카테고리 아이디 */
    private String categoryId;

    /* 게시판 카테고리 명 */
    private String categoryName;

    /* 순번 */
    private String orderNumber;

    /* 사용 여부 */
    private String useYn;
}
