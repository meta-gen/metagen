package com.koboolean.metagen.operationManagement.notice.controller;

import java.util.List;

import org.springdoc.core.converters.models.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.koboolean.metagen.board.domain.dto.BoardDto;
import com.koboolean.metagen.operationManagement.notice.service.NoticeService;

import groovy.util.logging.Slf4j;

import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequestMapping("/notice")
public class NoticeController {

    private NoticeService noticeService;

    /**
     * 공지사항 리스트 조회
     * @return String
     */
    @GetMapping("/noticeList")
    public String selectNoticeList(Model model) {

        List<BoardDto> selectNoticeList = noticeService.selectNoticeList();

        model.addAttribute("selectNoticeList", selectNoticeList);

        return "pages/notice/noticeList";
    }
}
