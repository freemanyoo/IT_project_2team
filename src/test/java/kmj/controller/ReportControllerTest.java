package kmj.controller;

import webproject_2team.lunch_matching.controller.ReportController;
import webproject_2team.lunch_matching.dto.ReportRequestDTO;
import webproject_2team.lunch_matching.domain.Board;
import webproject_2team.lunch_matching.domain.User;
import webproject_2team.lunch_matching.repository.BoardRepository;
import webproject_2team.lunch_matching.repository.ReportRepository;
import webproject_2team.lunch_matching.repository.UserRepository;
import webproject_2team.lunch_matching.service.BoardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.time.LocalDateTime;
import java.util.Optional;
import java.security.Principal;
import webproject_2team.lunch_matching.domain.Report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.argThat;
import org.mockito.ArgumentCaptor;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.spy;

@ExtendWith(MockitoExtension.class)
public class ReportControllerTest {

    @Mock
    private ReportRepository reportRepository;

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BoardService boardService;

    @InjectMocks
    private ReportController reportController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
    }

    @Test
    @DisplayName("1회 신고 시 사용자 신고 횟수 증가 테스트")
    void whenReportedOnce_thenReportCountIncrements() {
        // given
        User user = new User();
        user.setId(1L); // User ID 설정
        user.setUsername("testuser");
        user.setReportCount(0); // 초기 신고 횟수 0

        Board board = new Board();
        board.setId(1L);
        board.setUser(user);

        ReportRequestDTO reportRequestDTO = new ReportRequestDTO();
        reportRequestDTO.setBoardId(1L);

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user)); // userRepository.findById Mocking

        // userRepository.save가 호출될 때마다 user 객체의 reportCount를 업데이트하도록 설정
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            user.setReportCount(savedUser.getReportCount());
            return savedUser;
        });

        // when
        reportController.submitReport(reportRequestDTO, null, redirectAttributes, null);

        // then
        assertEquals(1, user.getReportCount()); // 1회 신고 후 reportCount가 1이 되는지 확인
    }

    @Test
    @DisplayName("3회 신고 시 게시글 블라인드 처리 테스트")
    void whenReportedThreeTimes_thenBoardIsBlinded() {
        // given
        User user = new User();
        user.setUsername("testuser");
        user.setReportCount(0);

        Board board = spy(new Board());
        board.setId(1L);
        board.setUser(user);
        board.setTitle("테스트 제목");
        board.setContent("테스트 내용");
        board.setBlinded(false); // 초기 상태는 블라인드 아님

        ReportRequestDTO reportRequestDTO = new ReportRequestDTO();
        reportRequestDTO.setBoardId(1L);
        reportRequestDTO.setReason("테스트 신고 사유");

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(boardService.filterBadWordsForUser(any(String.class))).thenReturn("필터링된 사유");
        when(boardService.checkBadWordsForAdmin(any(String.class))).thenReturn(null);

        // reportRepository.countReportsByBoardId 호출 시 1, 2, 3 순으로 반환하도록 설정
        when(reportRepository.countReportsByBoardId(1L)).thenReturn(1L, 2L, 3L);

        // userRepository.save가 호출될 때마다 user 객체의 reportCount를 업데이트하도록 설정
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            user.setReportCount(savedUser.getReportCount());
            return savedUser;
        });

        // boardRepository.save가 호출될 때 board 객체의 상태를 업데이트하도록 설정
        when(boardRepository.save(any(Board.class))).thenAnswer(invocation -> {
            Board savedBoard = invocation.getArgument(0);
            // Mock 객체의 상태를 직접 업데이트하지 않고, ArgumentCaptor를 통해 검증할 예정이므로 간단히 반환
            return savedBoard;
        });

        // when
        // 3회 신고 시뮬레이션
        reportController.submitReport(reportRequestDTO, null, redirectAttributes, null); // 1회 신고
        reportController.submitReport(reportRequestDTO, null, redirectAttributes, null); // 2회 신고
        reportController.submitReport(reportRequestDTO, null, redirectAttributes, null); // 3회 신고

        // then
        // boardRepository.save가 1번 호출되었는지 확인 (3회 신고 시에만 호출되므로)
        ArgumentCaptor<Board> boardCaptor = ArgumentCaptor.forClass(Board.class);
        verify(boardRepository, times(1)).save(boardCaptor.capture());

        // board.blindPost()가 1번 호출되었는지 확인
        verify(board, times(1)).blindPost(any(String.class));

        // 마지막으로 저장된 Board 객체의 상태 확인
        Board capturedBoard = boardCaptor.getValue();
        assertEquals(true, capturedBoard.isBlinded());
        assertNotNull(capturedBoard.getBlindReason());
        assertNotNull(capturedBoard.getBlindDate());
        assertNotNull(capturedBoard.getOriginalTitle());
        assertNotNull(capturedBoard.getOriginalContent());
    }

    @Test
    @DisplayName("각기 다른 유저가 3회 신고 시 게시글 블라인드 처리 테스트")
    void whenDifferentUsersReportThreeTimes_thenBoardIsBlinded() {
        // given
        User user = new User();
        user.setUsername("boardOwner");
        user.setReportCount(0);

        Board board = spy(new Board());
        board.setId(1L);
        board.setUser(user);
        board.setTitle("테스트 제목");
        board.setContent("테스트 내용");
        board.setBlinded(false); // 초기 상태는 블라인드 아님

        ReportRequestDTO reportRequestDTO = new ReportRequestDTO();
        reportRequestDTO.setBoardId(1L);
        reportRequestDTO.setReason("테스트 신고 사유");

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));
        when(boardService.filterBadWordsForUser(any(String.class))).thenReturn("필터링된 사유");
        when(boardService.checkBadWordsForAdmin(any(String.class))).thenReturn(null);

        // userRepository.save가 호출될 때마다 user 객체의 reportCount를 업데이트하도록 설정
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            user.setReportCount(savedUser.getReportCount());
            return savedUser;
        });

        // boardRepository.save가 호출될 때 board 객체의 상태를 업데이트하도록 설정
        when(boardRepository.save(any(Board.class))).thenAnswer(invocation -> {
            Board savedBoard = invocation.getArgument(0);
            // Mock 객체의 상태를 직접 업데이트하지 않고, ArgumentCaptor를 통해 검증할 예정이므로 간단히 반환
            return savedBoard;
        });

        // when
        // 1회 신고 (user1)
        Principal principal1 = () -> "user1";
        when(reportRepository.findByBoardIdAndReporter(1L, "user1")).thenReturn(Optional.empty());
        when(reportRepository.countReportsByBoardId(1L)).thenReturn(1L);
        reportController.submitReport(reportRequestDTO, principal1, redirectAttributes, null);

        // 2회 신고 (user2)
        Principal principal2 = () -> "user2";
        when(reportRepository.findByBoardIdAndReporter(1L, "user2")).thenReturn(Optional.empty());
        when(reportRepository.countReportsByBoardId(1L)).thenReturn(2L);
        reportController.submitReport(reportRequestDTO, principal2, redirectAttributes, null);

        // 3회 신고 (user3)
        Principal principal3 = () -> "user3";
        when(reportRepository.findByBoardIdAndReporter(1L, "user3")).thenReturn(Optional.empty());
        when(reportRepository.countReportsByBoardId(1L)).thenReturn(3L);
        reportController.submitReport(reportRequestDTO, principal3, redirectAttributes, null);

        // then
        // reportRepository.save가 3번 호출되었는지 확인
        verify(reportRepository, times(3)).save(any(Report.class));

        // boardRepository.save가 1번 호출되었는지 확인 (3회 신고 시에만 호출되므로)
        ArgumentCaptor<Board> boardCaptor = ArgumentCaptor.forClass(Board.class);
        verify(boardRepository, times(1)).save(boardCaptor.capture());

        // board.blindPost()가 1번 호출되었는지 확인
        verify(board, times(1)).blindPost(any(String.class));

        // 마지막으로 저장된 Board 객체의 상태 확인
        Board capturedBoard = boardCaptor.getValue();
        assertEquals(true, capturedBoard.isBlinded());
        assertNotNull(capturedBoard.getBlindReason());
        assertNotNull(capturedBoard.getBlindDate());
        assertNotNull(capturedBoard.getOriginalTitle());
        assertNotNull(capturedBoard.getOriginalContent());
    }
}
