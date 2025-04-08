package com.koboolean.metagen.operationManagement.notice.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.query.Page;
import org.springdoc.core.converters.models.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.koboolean.metagen.board.domain.dto.BoardDto;
import com.koboolean.metagen.board.domain.entity.Board;
import com.koboolean.metagen.operationManagement.notice.repository.NoticeRepository;
import com.koboolean.metagen.operationManagement.notice.service.NoticeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeServiceImpl implements NoticeService {

    private final NoticeRepository noticeRepository;

    /**
     *  공지사항 리스트 조회
     */
    public List<BoardDto> selectNoticeList() {

        //List<BoardDto> selectNoticeList = noticeRepository.findBy[ProjectId]();

        return null;
    }
}
