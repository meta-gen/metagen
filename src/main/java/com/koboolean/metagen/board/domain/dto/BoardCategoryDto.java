package com.koboolean.metagen.board.domain.dto;

import com.koboolean.metagen.board.domain.entity.BoardCategory;

import lombok.AllArgsConstructor;
import lombok.Builder;
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
    private int orderNumber;

    /* 사용 여부 */
    private char useYn;

    /* 빌더 호출 */
    public static BoardCategoryDto fromEntity(BoardCategory entity) {

        return BoardCategoryDto.builder()
                               .categoryId  (entity.getCategoryId()  )
                               .categoryName(entity.getCategoryName())
                               .orderNumber (entity.getOrderNumber() )
                               .useYn       (entity.getUseYn()       )
                               .build()
        ;
    }
}
