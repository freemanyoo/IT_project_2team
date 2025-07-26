package kmj.controller;

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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

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
        user.setUsername("testuser");
        user.setReportCount(0); // 초기 신고 횟수 0

        Board board = new Board();
        board.setId(1L);
        board.setUser(user);

        ReportRequestDTO reportRequestDTO = new ReportRequestDTO();
        reportRequestDTO.setBoardId(1L);

        RedirectAttributes redirectAttributes = new RedirectAttributesModelMap();

        when(boardRepository.findById(1L)).thenReturn(Optional.of(board));

        // userRepository.save가 호출될 때마다 user 객체의 reportCount를 업데이트하도록 설정
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            user.setReportCount(savedUser.getReportCount());
            return savedUser;
        });

        // when
        reportController.submitReport(reportRequestDTO, null, redirectAttributes);

        // then
        assertEquals(1, user.getReportCount()); // 1회 신고 후 reportCount가 1이 되는지 확인
    }
}
