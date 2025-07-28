package webproject_2team.lunch_matching;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import webproject_2team.lunch_matching.domain.Board;
import webproject_2team.lunch_matching.repository.BoardRepository;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.stream.IntStream;

@SpringBootTest
public class BoardDummyDataTest {

    @Autowired
    private BoardRepository boardRepository;

    private static final String[] regions = {"대연동", "광안리", "서면", "전포동"};
    private static final String[] foodCategories = {"한식", "중식", "일식", "분식", "디저트", "종류 안가림"};
    private static final String[] genderLimits = {"남", "여", "성별상관무"};

    @Test
    void insertDummyBoards() {
        Random random = new Random();

        IntStream.rangeClosed(1, 50).forEach(i -> {
            Board board = Board.builder()
                    .title("맛슐랭 테스트 모집글 " + i)
                    .content("이 글은 더미로 생성된 테스트용 모집글입니다. 번호: " + i)
                    .writer("user" + i)
                    .region(regions[random.nextInt(regions.length)])
                    .genderLimit(genderLimits[random.nextInt(genderLimits.length)])
                    .foodCategory(foodCategories[random.nextInt(foodCategories.length)])
                    .imagePath(null)
                    .createdAt(LocalDateTime.now().minusDays(random.nextInt(30))) // 최근 30일 랜덤
                    .build();

            boardRepository.save(board);
        });
    }
}
