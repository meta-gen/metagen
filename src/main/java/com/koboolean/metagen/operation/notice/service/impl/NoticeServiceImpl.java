package com.koboolean.metagen.operation.notice.service.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.koboolean.metagen.batch.config.BatchConfig;
import com.koboolean.metagen.board.domain.dto.BoardDto;
import com.koboolean.metagen.board.domain.entity.Board;
import com.koboolean.metagen.board.domain.entity.BoardCategory;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardDomainDto;
import com.koboolean.metagen.data.dictionary.domain.dto.StandardTermDto;
import com.koboolean.metagen.data.dictionary.domain.entity.StandardDomain;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.grid.enums.ColumnType;
import com.koboolean.metagen.grid.enums.RowType;
import com.koboolean.metagen.operation.notice.repository.BoardCategoryRepository;
import com.koboolean.metagen.operation.notice.repository.BoardRepository;
import com.koboolean.metagen.operation.notice.service.NoticeService;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.exception.CustomException;
import com.koboolean.metagen.security.exception.domain.ErrorCode;
import com.koboolean.metagen.utils.AuthUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService {
	
	Logger logger = LoggerFactory.getLogger(BatchConfig.class);

	private final BoardRepository boardRepository;
	private final BoardCategoryRepository boardCategoryRepository;

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

		Page<Board> allByProjectId = boardRepository.findAllByProjectId(accountDto.getProjectId(), pageable);


		return allByProjectId.map(BoardDto::fromEntity);
	}
	
	/**
	 * [공지사항 등록] 공지사항을 등록한다.
	 * 
	 * @param accountDto
	 * @param boardDto
	 */
    @Override
    @Transactional
    public void insertNotice(AccountDto accountDto, BoardDto boardDto) {

        boardDto.setProjectId(accountDto.getProjectId());
        
        BoardCategory boardCategory = boardCategoryRepository.findById("NOTICE").orElse(null);
        
        /* DB 데이터가 없는 경우 객체가 비어서 Error 발생하기 때문에 */
        if(boardCategory == null) {

        	// 예외 처리
        	throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }
        
        Board board = Board.builder()
        		.projectId    (boardDto.getProjectId() )
        		.boardCategory(boardCategory           )	// RDB에서 join의 개념으로 본다.
        		.title        (boardDto.getTitle()     )
        		.content      (boardDto.getContent()   )
        		.username     (accountDto.getUsername())
        		.deleteYn     ('Y'                     )
        		.hitCount     (0                       )
        		.build();
        
        boardRepository.save(board);
    }
}
