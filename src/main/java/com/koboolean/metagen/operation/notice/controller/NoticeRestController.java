package com.koboolean.metagen.operation.notice.controller;

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
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.operation.notice.service.NoticeService;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.utils.PageableUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@Tag(name = "공지사항 API", description = "공지사항 조회 및 관리 API")
@RequiredArgsConstructor
@RequestMapping("/api")
public class NoticeRestController {

    private final NoticeService noticeService;

    /**
     * 공지사항 리스트 데이터 조회
     * @param accountDto
     * @param page
     * @param size
     * @param sort
     * @param searchQuery
     * @param searchColumn
     * @return String
     */
    @Operation(summary = "공지사항 리스트 조회", description = "공지사항 리스트를 조회한다.")
    @GetMapping("/selectNotice/data")
    public ResponseEntity<Map<String,Object>> getNoticeList(@AuthenticationPrincipal AccountDto accountDto
                                                          , @RequestParam(value="page") int page
                                                          , @RequestParam(value="size") int size
                                                          , @RequestParam(value="sort", required = false) String sort
                                                          , @RequestParam(required = false, value = "searchQuery") String searchQuery
                                                          , @RequestParam(required = false, value = "searchColumn") String searchColumn) {

        Pageable pageable = PageableUtil.getGridPageable(page, size, sort);

        Page<BoardDto> selectnoticeListPage = noticeService.getNoticeList(pageable, accountDto);

        return PageableUtil.getGridPageableMap(selectnoticeListPage);
    }

    /**
     * 공지사항 리스트 컬럼 조회
     * @return
     */
    @Operation(summary = "공지사항 컬럼 조회", description = "공지사항 리스트의 컬럼을 조회한다.")
    @GetMapping("/selectNotice/column")
    public ResponseEntity<List<ColumnDto>> getNoticeListColumn() {

        return ResponseEntity.ok(noticeService.getNoticeListColumn());
    }
    
    /**
     * 공지사항 게시글 등록
     * @param accountDto
     * @param boardDto
     * @return
     */
    @Operation(summary = "공지사항 게시글 등록", description = "공지사항을 등록한다.")
    @PostMapping(value = "/saveNotice")
    public ResponseEntity<Map<String, Boolean>> insertNotice(@AuthenticationPrincipal AccountDto accountDto
    		                                               , @RequestBody BoardDto boardDto) {

    	noticeService.insertNotice(accountDto, boardDto);

        return ResponseEntity.ok(Map.of("result", true));
    }
}
