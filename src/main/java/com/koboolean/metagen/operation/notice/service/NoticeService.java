package com.koboolean.metagen.operation.notice.service;

import java.util.List;

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
    Page<BoardDto> getNoticeList(Pageable pageable, AccountDto accountDto);

    /**
     * 공지사항 컬럼 조회
     * @return
     */
    List<ColumnDto> getNoticeListColumn();
}