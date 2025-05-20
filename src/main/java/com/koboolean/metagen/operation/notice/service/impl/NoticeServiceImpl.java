package com.koboolean.metagen.operation.notice.service.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.koboolean.metagen.data.code.domain.dto.CodeRuleDetailDto;
import com.koboolean.metagen.security.domain.entity.Account;
import com.koboolean.metagen.security.repository.AccountRepository;
import com.koboolean.metagen.system.project.domain.dto.ProjectDto;
import com.koboolean.metagen.system.project.repository.ProjectMemberRepository;
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
	private final AccountRepository accountRepository;

	/**
	 * [공지사항 컬럼 조회] 공지사항 그리드에 표시할 컬럼을 조회한다.
	 * 
	 * @return
	 */
	@Override
	public List<ColumnDto> getNoticeListColumn(Long selectedId) {

		return List.of(new ColumnDto("", "id", ColumnType.NUMBER, RowType.CHECKBOX)
				     , new ColumnDto("게시글 제목", "title", ColumnType.STRING, RowType.TEXT, true, true)
				     , new ColumnDto("작성자", "username", ColumnType.STRING, RowType.TEXT, false, false)
				     , new ColumnDto("작성일", "updatedTime", ColumnType.DATE, RowType.TEXT, false, false)
				     , new ColumnDto("확인여부", "isHit", ColumnType.STRING, RowType.TEXT, false, false));
	}

	/**
	 * [공지사항 리스트 조회] 공지사항 그리드에 표시할 데이터를 조회한다.
	 *
	 * @param pageable
	 * @param selectedId
	 * @param accountId
	 * @param searchQuery
	 * @param searchColumn
	 */
	@Override
	public Page<BoardDto> getNoticeList(Pageable pageable, Long selectedId, String accountId, String searchQuery, String searchColumn) {

		if (searchQuery == null || searchQuery.trim().isEmpty()) {
			// 검색어가 없을 경우 전체 조회
			return boardRepository.findAllByProjectIdAndDeleteYn(selectedId, 'N', pageable).map(board -> BoardDto.fromEntity(board, accountId));
		}

		if(!searchQuery.isEmpty()) {
			searchQuery = "%" + searchQuery + "%";
		}

		Page<Board> allByProjectId = boardRepository.findAllByProjectIdAndTitleLike(selectedId, searchQuery, pageable);

		return allByProjectId.map(board -> BoardDto.fromEntity(board, accountId));
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

		Account account = accountRepository.findById(Long.valueOf(accountDto.getId())).orElse(null);

		boardDto.getProjectIds().forEach(projectId -> {
			// for문을 돌며 체크박스에 체크된 프로젝트 전부 공지사항을 등록한다.
			Board board = Board.builder()
					.projectId    (projectId)
					.boardCategory(boardCategory           )	// RDB에서 join의 개념으로 본다.
					.title        (boardDto.getTitle()     )
					.content      (boardDto.getContent()   )
					.username     (accountDto.getUsername())
					.deleteYn     ('N'                     )
					.updatedTime  (LocalDateTime.now()     )
					.accounts(new HashSet<>(List.of(account)))
					.projectIds(boardDto.getProjectIds())
					.build();

			boardRepository.save(board);
		});

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
		board.setProjectIds (boardDto.getProjectIds());

    }

	@Override
	@Transactional
	public BoardDto noticePopupMain(Long id, AccountDto accountDto) {

		Board board = boardRepository.findById(id).orElse(null);

		if(board == null) {
			board = new Board();
		}

		Account account = accountRepository.findById(Long.valueOf(accountDto.getId())).orElse(null);

		if(account != null && !board.getAccounts().contains(account)){
			board.getAccounts().add(account);
		}

		return BoardDto.fromEntity(board);
	}

	@Override
	@Transactional
	public void deleteNotice(AccountDto accountDto, List<BoardDto> boardDtos) {
		boardDtos.forEach(boardDto -> {
			// 연결되어있던 모든 공지사항이 함께 삭제된다.
			boardDto.getProjectIds().forEach(projectId -> {
				Board board = boardRepository.findByIdAndProjectId(boardDto.getId(), projectId).orElse(null);

				if(board != null){
					board.setDeleteYn('Y');
					board.setUpdatedTime(LocalDateTime.now());
				}
			});
		});
	}

	@Override
	public List<ProjectDto> selectAllProjectsByUsernameProjectManagerChecked(Long boardId, Long accountId, List<ProjectDto> collect) {

		Board board = boardRepository.findById(boardId).orElse(null);

		if(board != null) {
			BoardDto boardDto = BoardDto.fromEntity(board);

			collect.forEach(projectDto -> {
				boardDto.getProjectIds().forEach(projectId -> {
					Boolean isSelected = projectDto.getIsSelected();
					if(!isSelected){
						projectDto.setIsSelected(projectDto.getId().equals(projectId));
					}
				});
			});
		}

		return collect;
	}
}
