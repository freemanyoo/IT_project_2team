package webproject_2team.lunch_matching;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import webproject_2team.lunch_matching.domain.signup.Member;
import webproject_2team.lunch_matching.domain.signup.MemberRole;
import webproject_2team.lunch_matching.dto.ReviewDTO;
import webproject_2team.lunch_matching.repository.MemberRepository;
import webproject_2team.lunch_matching.service.ReviewService;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream; // IntStream 추가

@SpringBootTest
@lombok.extern.log4j.Log4j2
public class ReviewDummyDataTest {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testRegisterDummyReviews() { // 메서드 이름 변경
        log.info("Starting testRegisterDummyReviews...");

        // 1. 테스트용 멤버 조회 또는 생성
        String testUsername = "testuser123";
        String testNickname = "테스트닉네임";
        Optional<Member> memberOptional = memberRepository.findByUsername(testUsername);
        Member testMember;

        if (memberOptional.isPresent()) {
            testMember = memberOptional.get();
            log.info("Found existing test member: {}", testMember.getUsername());
        } else {
            // 멤버가 없으면 새로 생성
            testMember = Member.builder()
                    .username(testUsername)
                    .password(passwordEncoder.encode("1234")) // 비밀번호 암호화
                    .phoneNumber("01012345678")
                    .name("테스트사용자")
                    .gender("남")
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .email("test@example.com")
                    .nickname(testNickname)
                    .roles(Set.of(MemberRole.USER))
                    .build();
            memberRepository.save(testMember);
            log.info("Created new test member: {}", testMember.getUsername());
        }

        // 2. 100개의 더미 리뷰 생성 및 등록
        IntStream.rangeClosed(1, 100).forEach(i -> {
            String content = String.format("이것은 %d번째 더미 리뷰 내용입니다. 오늘 점심은 정말 좋았습니다. " +
                    "맛있는 음식과 즐거운 분위기 덕분에 행복한 시간을 보냈습니다. " +
                    "특히 %s 메뉴는 %s에서 먹었는데, 정말 인상 깊었습니다. " +
                    "다음에 또 방문하고 싶네요. 이 리뷰는 테스트를 위해 작성되었습니다.",
                    i, (i % 3 == 0 ? "파스타" : (i % 3 == 1 ? "스테이크" : "샐러드")),
                    (i % 2 == 0 ? "강남점" : "홍대점"));

            String menu = String.format("메뉴 %d", i);
            String place = String.format("장소 %d", i);
            int rating = (i % 5) + 1; // 1부터 5까지의 평점
            String emotion = "emotion" + (i % 9); // emotion0부터 emotion8까지

            ReviewDTO reviewDTO = ReviewDTO.builder()
                    .member_id(testMember.getUsername())
                    .nickname(testMember.getNickname())
                    .content(content)
                    .menu(menu)
                    .place(place)
                    .rating(rating)
                    .emotion(emotion)
                    .build();

            Long reviewId = reviewService.register(reviewDTO);
            log.info("Registered dummy review {} with ID: {}", i, reviewId);
        });

        log.info("Finished registering 100 dummy reviews.");
    }
}
