package com.koboolean.metagen.operationManagement.notice.service;

import java.util.List;

import com.koboolean.metagen.board.domain.dto.BoardDto;

public interface NoticeService {

    /**
     * 공지사항 작성하기.
     * @return
     */
    public List<BoardDto> selectNoticeList();
}