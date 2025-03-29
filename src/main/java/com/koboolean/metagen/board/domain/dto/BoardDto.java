package com.koboolean.metagen.board.domain.dto;

import groovy.transform.builder.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Builder
@Accessors(chain = true)
@AllArgsConstructor
public class BoardDto {

    /* 게시글 번호 */
    private long id;

    /* 회원 아이디 */
    private String username;

    /* 게시판 카테고리 아이디 */
    private String categoryId;

    /* 게시글 제목 */
    private String title;

    /* 게시글 내용 */
    private String content;

    /* 조회수 */
    private int hitCount;

    /* 게시글 상태 */
    private String status;

    /* 삭제 여부 */
    private String deleteYn;
}
