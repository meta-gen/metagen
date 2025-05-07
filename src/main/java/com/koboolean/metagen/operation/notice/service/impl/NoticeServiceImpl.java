package com.koboolean.metagen.operation.notice.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.koboolean.metagen.batch.config.BatchConfig;
import com.koboolean.metagen.board.domain.dto.BoardDto;
import com.koboolean.metagen.board.domain.entity.Board;
import com.koboolean.metagen.board.domain.entity.BoardCategory;
import com.koboolean.metagen.grid.domain.dto.ColumnDto;
import com.koboolean.metagen.grid.enums.ColumnType;
import com.koboolean.metagen.grid.enums.RowType;
import com.koboolean.metagen.operation.notice.repository.BoardCategoryRepository;
import com.koboolean.metagen.operation.notice.repository.BoardRepository;
import com.koboolean.metagen.operation.notice.service.NoticeService;
import com.koboolean.metagen.security.domain.dto.AccountDto;
import com.koboolean.metagen.security.exception.CustomException;
import com.koboolean.metagen.security.exception.domain.ErrorCode;

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
	public List<ColumnDto> getNoticeListColumn(Long selectedId) {

		return List.of(new ColumnDto("", "id", ColumnType.NUMBER, RowType.CHECKBOX)
				     , new ColumnDto("게시글 제목", "categoryName", ColumnType.STRING, true)
				     , new ColumnDto("작성자", "username", ColumnType.STRING, true)
				     , new ColumnDto("작성일", "updatedTime", ColumnType.DATE, RowType.TEXT)
				     , new ColumnDto("조회수", "hitCount", ColumnType.STRING));
	}

	/**
	 * [공지사항 리스트 조회] 공지사항 그리드에 표시할 데이터를 조회한다.
	 *
	 * @param pageable
	 * @param selectedId
	 */
	@Override
	public Page<BoardDto> getNoticeList(Pageable pageable, Long selectedId) {

		Page<Board> allByProjectId = boardRepository.findAllByProjectId(selectedId, pageable);


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
        		.deleteYn     ('N'                     )
        		.hitCount     (0                       )
        		.updatedTime  (LocalDateTime.now()     )
        		.build();
        
        boardRepository.save(board);
    }
    
	/**
	 * [공지사항 수정] 공지사항을 수정한다.
	 * 
	 * @param accountDto
	 * @param boardDto
	 */
    @Override
    @Transactional
    public void updateNotice(AccountDto accountDto, BoardDto boardDto) {

        boardDto.setProjectId(accountDto.getProjectId());
        
        BoardCategory boardCategory = boardCategoryRepository.findById("NOTICE").orElse(null);
        
        /* DB 데이터가 없는 경우 객체가 비어서 Error 발생하기 때문에 */
        if(boardCategory == null) {

        	// 예외 처리
        	throw new CustomException(ErrorCode.DATA_CANNOT_BE_DELETED);
        }
        
        /* update의 경우 save로 저장하지 않고 객체에 값을 바꾼다.
         * 객체에 값을 바꾸면 JPA 정책 상 데이터를 덮어써서 트랜잭션이 마무리되는 시점에 flush 한다.
         */
        Board board = boardRepository.findById(boardDto.getId()).orElse(null);
        
        board.setTitle      (boardDto.getTitle()      );
        board.setContent    (boardDto.getContent()    );
        board.setUpdatedTime(LocalDateTime.now()	  );
    }

	@Override
	@Transactional
	public BoardDto noticePopupMain(Long id) {
		Board board = boardRepository.findById(id).orElse(null);

		if(board == null) {
			board = new Board();
		}

		board.setHitCount(board.getHitCount() + 1);

		return BoardDto.fromEntity(board);
	}

	@Override
	@Transactional
	public BoardDto noticeSavePopup(Long id) {
		Board board = boardRepository.findById(id).orElse(null);

		if(board == null) {
			board = new Board();
		}

		return BoardDto.fromEntity(board);
	}
}
