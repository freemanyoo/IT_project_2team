package kmj.service;

import webproject_2team.lunch_matching.service.BoardService;
import webproject_2team.lunch_matching.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BoardServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private BoardService boardService;

    @BeforeEach
    void setUp() {
        boardService.init(); // @PostConstruct 메서드를 수동으로 호출
    }

    @Test
    @DisplayName("신고가 3회 미만일 때 게시글은 블라인드 처리되지 않는다")
    void whenReportCountIsLessThanThree_thenBoardShouldNotBeBlinded() {
        // given
        Long boardId = 1L;
        when(reportRepository.countReportsByBoardId(boardId)).thenReturn(2L);

        // when
        boolean isBlinded = boardService.isBlindedBoard(boardId);

        // then
        assertFalse(isBlinded);
    }

    @Test
    @DisplayName("신고가 3회 이상일 때 게시글은 블라인드 처리된다")
    void whenReportCountIsThreeOrMore_thenBoardShouldBeBlinded() {
        // given
        Long boardId = 1L;
        when(reportRepository.countReportsByBoardId(boardId)).thenReturn(3L);

        // when
        boolean isBlinded = boardService.isBlindedBoard(boardId);

        // then
        assertTrue(isBlinded);
    }


        @Test
        public void 사용자출력_테스트() {
            String input = "넌 진짜 개새끼야";
            String expected = "넌 진짜 ***야";
            String result = boardService.filterBadWordsForUser(input);

            System.out.println("사용자 출력: " + result);
            assertEquals(expected, result);
        }

           @Test
        public void 씨발_욕설_테스트() {
            String input = "씨발놈아";
            String expectedUserOutput = "**놈아";  // 씨발(2글자) -> **(2개 별표)
            String expectedAdminOutput = "씨발가 포함된 문장입니다";

            String userOutput = boardService.filterBadWordsForUser(input);
            String adminOutput = boardService.checkBadWordsForAdmin(input);

            System.out.println("입력: " + input);
            System.out.println("사용자 출력: " + userOutput);
            System.out.println("관리자 메시지: " + adminOutput);

            assertEquals(expectedUserOutput, userOutput);
            assertEquals(expectedAdminOutput, adminOutput);
        }

        @Test
        public void 관리자_원본텍스트_DB저장_테스트() {
            String input = "씨발놈아";  // 단일 비속어로 테스트

            // 사용자에게는 마스킹된 텍스트
            String userOutput = boardService.filterBadWordsForUser(input);
            System.out.println("사용자 출력: " + userOutput);

            // 관리자는 비속어 감지 메시지
            String adminMessage = boardService.checkBadWordsForAdmin(input);
            System.out.println("관리자 알림: " + adminMessage);

            // DB에는 원본 텍스트 저장
            String originalForDB = boardService.getOriginalTextForAdmin(input);
            System.out.println("DB 저장용 원본: " + originalForDB);

            assertEquals("**놈아", userOutput);
            assertEquals("씨발가 포함된 문장입니다", adminMessage);
            assertEquals(input, originalForDB); // 원본 그대로
            assertTrue(boardService.containsBadWords(input));
        }



        @Test
        public void 비속어가_없는_경우_테스트() {
            String input = "안녕하세요 좋은 하루입니다";
            String userResult = boardService.filterBadWordsForUser(input);
            String adminResult = boardService.checkBadWordsForAdmin(input);

            assertEquals(input, userResult); // 변화 없음
            assertNull(adminResult); // null 반환
            assertFalse(boardService.containsBadWords(input));
        }

        @Test
        public void 복합_비속어_테스트() {
            String input = "미친새끼 병신아";
            String userOutput = boardService.filterBadWordsForUser(input);
            String adminOutput = boardService.checkBadWordsForAdmin(input);

            System.out.println("입력: " + input);
            System.out.println("사용자 출력: " + userOutput);
            System.out.println("관리자 메시지: " + adminOutput);

            // 미친새끼(4글자) -> ****, 병신(2글자) -> **
            assertEquals("**** **아", userOutput);
            assertNotNull(adminOutput);
            assertTrue(boardService.containsBadWords(input));
        }
    }