package com.koboolean.metagen.operation.notice.service;

import java.util.List;

import com.koboolean.metagen.system.project.domain.dto.ProjectDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.koboolean.metagen.board.domain.dto.BoardDto;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.security.domain.dto.AccountDto;

public interface NoticeService {

    /**
     * 공지사항 리스트 조회
     * @return
     */
    Page<BoardDto> getNoticeList(Pageable pageable, Long selectedId, String accountId, String searchQuery, String searchColumn);

    /**
     * 공지사항 컬럼 조회
     * @return
     */
    List<ColumnDto> getNoticeListColumn(Long selectedId);
    
    /**
     * 공지사항 등록
     * @param accountDto
     * @param boardDto
     */
    void insertNotice(AccountDto accountDto, BoardDto boardDto);
    
    /**
     * 공지사항 수정
     * @param accountDto
     * @param boardDto
     */
    void updateNotice(AccountDto accountDto, BoardDto boardDto);

    /**
     * 공지사항 상세조회
     * @param id
     * @return
     */
    BoardDto noticePopupMain(Long id, AccountDto accountDto);

    /**
     * 공지사항 삭제
     * @param accountDto
     * @param boardDtos
     * @return
     */
    void deleteNotice(AccountDto accountDto, List<BoardDto> boardDtos);

    /**
     * 공지사항 프로젝트별 정보를 수정하기위한 프로젝트를 조회한다.
     * @param boardId
     * @param accountId
     * @param collect
     * @return
     */
    List<ProjectDto> selectAllProjectsByUsernameProjectManagerChecked(Long boardId, Long accountId, List<ProjectDto> collect);
}
