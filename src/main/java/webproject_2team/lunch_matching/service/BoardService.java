package webproject_2team.lunch_matching.service;

import com.example.kmj.repository.ReportRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 게시글 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 비속어 필터링, 게시글 블라인드 여부 확인 등의 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
public class BoardService {

    private final ReportRepository reportRepository;
    private List<String> badWords;

    /**
     * 서비스 초기화 시 비속어 목록을 로드합니다.
     * `@PostConstruct` 어노테이션을 사용하여 빈 생성 후 자동으로 호출됩니다.
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        loadBadWords();
    }

    /**
     * 특정 게시글이 블라인드 처리되었는지 여부를 확인합니다.
     * 신고 횟수가 3회 이상이면 블라인드 처리된 것으로 간주합니다.
     * @param boardId 확인할 게시글의 ID
     * @return 게시글이 블라인드 처리되었으면 true, 아니면 false
     */
    public boolean isBlindedBoard(Long boardId) {
        Long reportCount = reportRepository.countReportsByBoardId(boardId);
        return reportCount >= 3;
    }

    /**
     * 특정 게시글의 현재 신고 횟수를 조회합니다.
     * @param boardId 조회할 게시글의 ID
     * @return 해당 게시글의 신고 횟수
     */
    public Long getReportCount(Long boardId) {
        return reportRepository.countReportsByBoardId(boardId);
    }

    /**
     * 입력된 문자열에서 비속어를 필터링하여 사용자에게 표시할 수 있도록 별표(*)로 마스킹합니다.
     * 긴 비속어부터 처리하여 부분 문자열 문제를 방지합니다.
     * @param input 필터링할 원본 문자열
     * @return 비속어가 마스킹된 문자열
     */
    public String filterBadWordsForUser(String input) {
        if (badWords == null || badWords.isEmpty()) return input;

        String result = input;

        // 긴 비속어부터 처리 (부분 문자열 문제 방지)
        List<String> sortedBadWords = new ArrayList<>(badWords);
        sortedBadWords.sort(Comparator.comparingInt(String::length).reversed());

        for (String badWord : sortedBadWords) {
            if (result.contains(badWord)) {
                String stars = "*".repeat(badWord.length());
                result = result.replace(badWord, stars);
            }
        }
        return result;
    }

    /**
     * 입력된 문자열에 비속어가 포함되어 있는지 확인하고, 관리자에게 알릴 메시지를 반환합니다.
     * 비속어가 포함되어 있지 않으면 null을 반환합니다.
     * @param input 확인할 원본 문자열
     * @return 비속어가 포함된 경우 해당 비속어와 함께 알림 메시지, 없으면 null
     */
    public String checkBadWordsForAdmin(String input) {
        if (badWords == null || badWords.isEmpty()) return null;

        for (String badWord : badWords) {
            if (input.contains(badWord)) {
                return badWord + "가 포함된 문장입니다";
            }
        }
        return null;
    }

    /**
     * 관리자용으로 원본 텍스트를 반환합니다.
     * 데이터베이스 저장 시 원본 텍스트를 그대로 사용해야 할 경우 호출됩니다.
     * @param input 원본 문자열
     * @return 입력된 원본 문자열
     */
    public String getOriginalTextForAdmin(String input) {
        return input; // 원본 그대로 반환
    }

    /**
     * 입력된 문자열에 비속어가 포함되어 있는지 여부를 확인합니다.
     * @param input 확인할 문자열
     * @return 비속어가 포함되어 있으면 true, 아니면 false
     */
    public boolean containsBadWords(String input) {
        if (badWords == null || badWords.isEmpty()) return false;

        for (String badWord : badWords) {
            if (input.contains(badWord)) {
                return true;
            }
        }
        return false;
    }

    /**
     * `badwords.txt` 파일에서 비속어 목록을 로드합니다.
     * 파일 로드 실패 시 기본 비속어 목록을 사용합니다.
     */
    private void loadBadWords() {
        badWords = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/static/badwords.txt"), "UTF-8"))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String word = line.trim();
                if (!word.isEmpty()) {
                    badWords.add(word);
                }
            }
            System.out.println("비속어 목록 로드 완료: " + badWords.size() + "개");

        } catch (Exception e) {
            System.err.println("badwords.txt 파일을 읽을 수 없습니다: " + e.getMessage());
            // 테스트용 기본 비속어 추가
            badWords.add("개새끼");
            badWords.add("씨발");
            System.out.println("기본 비속어 목록을 사용합니다.");
        }
    }
}