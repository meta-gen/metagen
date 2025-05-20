package com.koboolean.metagen.board.domain.dto;

import com.koboolean.metagen.security.domain.dto.AccountDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.koboolean.metagen.board.domain.entity.Board;

import lombok.experimental.Accessors;

@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BoardDto {

    /* 게시글 번호 */
    private Long id;

    /* 프로젝트 아이디 */
    private Long projectId;

    /* 회원 아이디 */
    private String username;

    /* 게시판 카테고리 아이디 */
    private String categoryId;
    
    /* 게시판 카테고리 명 */
    private String categoryName;

    /* 게시글 제목 */
    private String title;

    /* 게시글 내용 */
    private String content;

    /* 삭제 여부 */
    private char deleteYn;
    
    private LocalDateTime updatedTime;
    
    private BoardCategoryDto boardCategoryDto;

    private Set<AccountDto> accounts = new HashSet<>();

    private String isHit;

    private List<Long> projectIds;
    
    /* 빌더 호출 */
    public static BoardDto fromEntity(Board entity) {
    	
    	BoardCategoryDto boardCategoryDto = BoardCategoryDto.fromEntity(entity.getBoardCategory());

        Set<AccountDto> collect = entity.getAccounts().stream().map(AccountDto::fromEntity).collect(Collectors.toSet());

        return BoardDto.builder()
                       .id          (entity.getId()       )
                       .projectId   (entity.getProjectId())
                       .username    (entity.getUsername() )
                       .title       (entity.getTitle()    )
                       .content     (entity.getContent()  )
                       .deleteYn    (entity.getDeleteYn() )
                       .updatedTime (entity.getUpdatedTime())
                       .categoryName(boardCategoryDto.getCategoryName())
                       .categoryId  (boardCategoryDto.getCategoryId())
                       .accounts    (collect)
                       .isHit("")
                       .build()
        ;
    }

    /* 빌더 호출 */
    public static BoardDto fromEntity(Board entity, String accountId) {

        BoardCategoryDto boardCategoryDto = BoardCategoryDto.fromEntity(entity.getBoardCategory());

        Set<AccountDto> collect = entity.getAccounts().stream().map(AccountDto::fromEntity).collect(Collectors.toSet());

        Boolean isHits = false;

        for(AccountDto account : collect) {
            if(account.getId().equals(accountId)){
                isHits = true;
            }
        }

        return BoardDto.builder()
                .id          (entity.getId()       )
                .projectId   (entity.getProjectId())
                .username    (entity.getUsername() )
                .title       (entity.getTitle()    )
                .content     (entity.getContent()  )
                .deleteYn    (entity.getDeleteYn() )
                .updatedTime (entity.getUpdatedTime())
                .categoryName(boardCategoryDto.getCategoryName())
                .categoryId  (boardCategoryDto.getCategoryId())
                .accounts    (collect)
                .isHit(isHits ? "확인" : "미확인")
                .build()
                ;
    }
}
