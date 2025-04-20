package com.koboolean.metagen.operation.notice.service.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.koboolean.metagen.board.domain.dto.BoardDto;
import com.koboolean.metagen.board.domain.entity.Board;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.grid.enums.ColumnType;
import com.koboolean.metagen.grid.enums.RowType;
import com.koboolean.metagen.operation.notice.repository.NoticeRepository;
import com.koboolean.metagen.operation.notice.service.NoticeService;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.utils.AuthUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {

	private final NoticeRepository noticeRepository;

	/**
	 * [공지사항 컬럼 조회] 공지사항 그리드에 표시할 컬럼을 조회한다.
	 * 
	 * @return
	 */
	@Override
	public List<ColumnDto> getNoticeListColumn() {

		return List.of(new ColumnDto("", "id", ColumnType.NUMBER, RowType.CHECKBOX)
				     , new ColumnDto("게시글 제목", "categoryName", ColumnType.STRING, true)
				     , new ColumnDto("작성자", "username", ColumnType.STRING, true)
				     , new ColumnDto("작성일", "created", ColumnType.STRING)
				     , new ColumnDto("조회수", "hitCount", ColumnType.NUMBER));
	}

	/**
	 * [공지사항 리스트 조회] 공지사항 그리드에 표시할 데이터를 조회한다.
	 * 
	 * @param pageable
	 * @param accountDto
	 */
	@Override
	public Page<BoardDto> getNoticeList(Pageable pageable, AccountDto accountDto) {

		Page<Board> allByProjectId = noticeRepository.findAllByProjectId(accountDto.getProjectId(), pageable);


		return allByProjectId.map(BoardDto::fromEntity);
	}
	
	/**
	 * [공지사항 등록] 공지사항을 등록한다.
	 * 
	 * @param accountDto
	 * @param boardDto
	 */
    @Override
    public void insertNotice(AccountDto accountDto, BoardDto boardDto) {

        boardDto.setProjectId(accountDto.getProjectId());
        
        Board board = Board.fromEntity(boardDto);

        noticeRepository.save(board);
    }
}
