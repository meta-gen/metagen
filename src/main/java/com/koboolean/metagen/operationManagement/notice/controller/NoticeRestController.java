package com.koboolean.metagen.operationManagement.notice.controller;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.koboolean.metagen.board.domain.dto.BoardDto;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.operationManagement.notice.service.NoticeService;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.utils.PageableUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;

@RestController
@Tag(name = "공지사항 API", description = "공지사항 조회 및 관리 API")
@RequestMapping("/api")
public class NoticeRestController {

    private NoticeService noticeService;

    /**
     * 공지사항 리스트 조회
     * @return String
     */
    @Operation(summary = "공지사항 리스트 조회", description = "공지사항 리스트를 조회한다.")
    @GetMapping("/getNoticeList/data")
    public ResponseEntity<Map<String,Object>> getNoticeList(@AuthenticationPrincipal AccountDto accountDto
                                                          , @RequestParam int page
                                                          , @RequestParam int size
                                                          , @RequestParam(required = false) String sort) {

        Pageable pageable = PageableUtil.getGridPageable(page, size, sort);

        Page<BoardDto> selectnoticeListPage = noticeService.getNoticeList(pageable, accountDto);

        return PageableUtil.getGridPageableMap(selectnoticeListPage);
    }

    @Operation(summary = "공지사항 컬럼 조회", description = "공지사항 리스트의 컬럼을 조회한다.")
    @GetMapping("/getNoticeList/column")
    public ResponseEntity<List<ColumnDto>> getNoticeListColumn() {

        return ResponseEntity.ok(noticeService.getNoticeListColumn());
    }
}
