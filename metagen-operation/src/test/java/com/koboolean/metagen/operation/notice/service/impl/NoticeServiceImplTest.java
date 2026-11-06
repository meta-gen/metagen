package com.koboolean.metagen.operation.notice.service.impl;

import com.koboolean.metagen.common.exception.CustomException;
import com.koboolean.metagen.common.exception.domain.ErrorCode;
import com.koboolean.metagen.core.project.domain.dto.ProjectDto;
import com.koboolean.metagen.core.security.domain.dto.AccountDto;
import com.koboolean.metagen.core.security.domain.entity.Account;
import com.koboolean.metagen.core.security.repository.AccountRepository;
import com.koboolean.metagen.core.support.CoreFixtures;
import com.koboolean.metagen.operation.board.domain.dto.BoardDto;
import com.koboolean.metagen.operation.board.domain.entity.Board;
import com.koboolean.metagen.operation.board.domain.entity.BoardCategory;
import com.koboolean.metagen.operation.notice.repository.BoardCategoryRepository;
import com.koboolean.metagen.operation.notice.repository.BoardRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NoticeServiceImplTest {

    @Mock
    BoardRepository boardRepository;
    @Mock
    BoardCategoryRepository boardCategoryRepository;
    @Mock
    AccountRepository accountRepository;

    @InjectMocks
    NoticeServiceImpl service;

    private final AccountDto login = AccountDto.builder().id("1").username("manager").projectId(1L).build();

    private static BoardCategory noticeCategory() {
        return new BoardCategory("NOTICE", "공지사항", 1, 'Y', new ArrayList<>());
    }

    private static Board board(Long id, List<Long> projectIds) {
        return Board.builder()
                .id(id)
                .projectId(projectIds.get(0))
                .title("제목")
                .content("내용")
                .username("manager")
                .deleteYn('N')
                .boardCategory(noticeCategory())
                .accounts(new HashSet<>())
                .projectIds(new ArrayList<>(projectIds))
                .build();
    }

    @Nested
    @DisplayName("공지 등록/수정")
    class Save {

        @Test
        @DisplayName("NOTICE 카테고리가 없으면 등록할 수 없다")
        void noCategory() {
            when(boardCategoryRepository.findById("NOTICE")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.insertNotice(login, BoardDto.builder().projectIds(List.of(1L)).build()))
                    .isInstanceOf(CustomException.class)
                    .extracting("errorCode").isEqualTo(ErrorCode.DATA_CANNOT_BE_DELETED);
            verify(boardRepository, never()).save(any());
        }

        @Test
        @DisplayName("체크한 대상 프로젝트마다 공지를 한 건씩 등록하고, 작성자는 읽음 처리한다")
        void insertPerProject() {
            Account writer = CoreFixtures.account(1L, "manager");
            when(boardCategoryRepository.findById("NOTICE")).thenReturn(Optional.of(noticeCategory()));
            when(accountRepository.findById(1L)).thenReturn(Optional.of(writer));

            service.insertNotice(login, BoardDto.builder().title("점검 안내").content("내용").projectIds(List.of(1L, 2L, 3L)).build());

            ArgumentCaptor<Board> saved = ArgumentCaptor.forClass(Board.class);
            verify(boardRepository, times(3)).save(saved.capture());
            assertThat(saved.getAllValues()).extracting(Board::getProjectId).containsExactly(1L, 2L, 3L);
            assertThat(saved.getAllValues()).allSatisfy(b -> {
                assertThat(b.getTitle()).isEqualTo("점검 안내");
                assertThat(b.getDeleteYn()).isEqualTo('N');
                assertThat(b.getUsername()).isEqualTo("manager");
                assertThat(b.getAccounts()).containsExactly(writer);
                assertThat(b.getProjectIds()).containsExactly(1L, 2L, 3L);
            });
        }

        @Test
        @DisplayName("수정은 제목/내용/대상 프로젝트를 변경한다")
        void update() {
            Board board = board(10L, List.of(1L));
            when(boardCategoryRepository.findById("NOTICE")).thenReturn(Optional.of(noticeCategory()));
            when(boardRepository.findById(10L)).thenReturn(Optional.of(board));

            service.updateNotice(login, BoardDto.builder().id(10L).title("새 제목").content("새 내용").projectIds(List.of(1L, 4L)).build());

            assertThat(board.getTitle()).isEqualTo("새 제목");
            assertThat(board.getContent()).isEqualTo("새 내용");
            assertThat(board.getProjectIds()).containsExactly(1L, 4L);
            assertThat(board.getUpdatedTime()).isNotNull();
        }
    }

    @Test
    @DisplayName("목록 검색은 삭제 여부 대신 제목 LIKE 검색을 사용한다")
    void searchList() {
        Pageable pageable = PageRequest.of(0, 10);
        when(boardRepository.findAllByProjectIdAndDeleteYn(1L, 'N', pageable)).thenReturn(Page.empty());
        when(boardRepository.findAllByProjectIdAndTitleLike(1L, "%점검%", pageable)).thenReturn(Page.empty());

        service.getNoticeList(pageable, 1L, "1", " ", null);
        service.getNoticeList(pageable, 1L, "1", "점검", "title");

        verify(boardRepository).findAllByProjectIdAndDeleteYn(1L, 'N', pageable);
        verify(boardRepository).findAllByProjectIdAndTitleLike(1L, "%점검%", pageable);
    }

    @Test
    @DisplayName("목록에서 로그인 사용자가 읽은 공지는 '확인', 아니면 '미확인'")
    void hitFlag() {
        Pageable pageable = PageRequest.of(0, 10);
        Board read = board(10L, List.of(1L));
        read.getAccounts().add(CoreFixtures.account(1L, "manager", CoreFixtures.role(1L, "ROLE_USER")));
        Board unread = board(11L, List.of(1L));
        when(boardRepository.findAllByProjectIdAndDeleteYn(1L, 'N', pageable))
                .thenReturn(new org.springframework.data.domain.PageImpl<>(List.of(read, unread)));

        assertThat(service.getNoticeList(pageable, 1L, "1", null, null).getContent())
                .extracting(BoardDto::getIsHit).containsExactly("확인", "미확인");
    }

    @Test
    @DisplayName("삭제는 로그인 프로젝트의 공지만 논리 삭제하고 대상 프로젝트에서 제외한다")
    void deleteNotice() {
        Board board = board(10L, List.of(1L, 2L));
        when(boardRepository.findByIdAndProjectId(10L, 1L)).thenReturn(Optional.of(board));
        when(boardRepository.findByIdAndProjectId(99L, 1L)).thenReturn(Optional.empty());

        service.deleteNotice(login, List.of(BoardDto.builder().id(10L).build(), BoardDto.builder().id(99L).build()));

        assertThat(board.getDeleteYn()).isEqualTo('Y');
        assertThat(board.getProjectIds()).containsExactly(2L);
    }

    @Test
    @DisplayName("상세 팝업을 열면 조회자를 읽음 목록에 한 번만 추가한다")
    void popupMarksRead() {
        Board board = board(10L, List.of(1L));
        Account reader = CoreFixtures.account(1L, "manager", CoreFixtures.role(1L, "ROLE_USER"));
        when(boardRepository.findById(10L)).thenReturn(Optional.of(board));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(reader));

        service.noticePopupMain(10L, login);
        BoardDto dto = service.noticePopupMain(10L, login);

        assertThat(board.getAccounts()).containsExactly(reader);
        assertThat(dto.getTitle()).isEqualTo("제목");
    }

    @Test
    @DisplayName("수정 팝업 : 관리 프로젝트 중 공지 대상 프로젝트에 체크 표시한다")
    void checkedProjects() {
        when(boardRepository.findById(10L)).thenReturn(Optional.of(board(10L, List.of(2L, 3L))));
        List<ProjectDto> managed = List.of(
                ProjectDto.builder().id(1L).isSelected(false).build(),
                ProjectDto.builder().id(2L).isSelected(false).build(),
                ProjectDto.builder().id(3L).isSelected(false).build());

        List<ProjectDto> result = service.selectAllProjectsByUsernameProjectManagerChecked(10L, 1L, managed);

        assertThat(result).extracting(ProjectDto::getIsSelected).containsExactly(false, true, true);
    }
}
