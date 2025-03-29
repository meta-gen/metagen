package com.koboolean.metagen.operationManagement.notice.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.koboolean.metagen.board.domain.dto.BoardDto;
import com.koboolean.metagen.board.domain.entity.Board;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.grid.enums.ColumnType;
import com.koboolean.metagen.operationManagement.notice.repository.NoticeRepository;
import com.koboolean.metagen.operationManagement.notice.service.NoticeService;
import com.koboolean.metagen.security.domain.dto.AccountDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;

    /** [공지사항 컬럼 조회]
     *  공지사항 그리드에 표시할 컬럼을 조회한다.
     *  @return
     */
    @Override
    public List<ColumnDto> getNoticeListColumn() {

        return List.of(new ColumnDto("게시글 번호", "id"        , ColumnType.NUMBER)
                     , new ColumnDto("게시글 제목", "title"     , ColumnType.STRING)
                    );
    }

    /** [공지사항 리스트 조회]
     *  공지사항 그리드에 표시할 데이터를 조회한다.
     */
    @Override
    public Page<BoardDto> getNoticeList(Pageable pageable, AccountDto accountDto) {

        Page<Board> allByProjectId = noticeRepository.findAllByProjectId(accountDto.getProjectId(), pageable);

        return allByProjectId.map(BoardDto::fromEntity);
    }
}
