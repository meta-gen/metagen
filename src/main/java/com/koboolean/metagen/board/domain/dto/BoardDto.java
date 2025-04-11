package com.koboolean.metagen.board.domain.dto;

import lombok.*;

import com.koboolean.metagen.board.domain.entity.Board;

import lombok.experimental.Accessors;

@Data
@Builder
@Getter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class BoardDto {

    /* 게시글 번호 */
    private long id;

    /* 프로젝트 아이디 */
    private Long projectId;

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

    /* 삭제 여부 */
    private char deleteYn;

    /* 빌더 호출 */
    public static BoardDto fromEntity(Board entity) {

        return BoardDto.builder()
                       .id       (entity.getId()       )
                       .projectId(entity.getProjectId())
                       .username (entity.getUsername() )
                       .title    (entity.getTitle()    )
                       .content  (entity.getContent()  )
                       .hitCount (entity.getHitCount() )
                       .deleteYn (entity.getDeleteYn() )
                       .build()
        ;
    }
}
