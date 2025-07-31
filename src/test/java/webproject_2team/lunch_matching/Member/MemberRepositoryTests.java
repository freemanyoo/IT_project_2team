package webproject_2team.lunch_matching.Member;

import webproject_2team.lunch_matching.dto.signup.MemberSignupDTO;

import webproject_2team.lunch_matching.service.signup.MemberService;
import webproject_2team.lunch_matching.service.signup.EmailAuthService; // EmailAuthService 임포트
import jakarta.transaction.Transactional; // jakarta.transaction.Transactional 임포트 확인
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
//import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.anyString; // anyString 임포트
import static org.mockito.Mockito.when; // when 임포트
@Log4j2
@SpringBootTest
public class MemberRepositoryTests {

    @Autowired
    private MemberService memberService;

//    @MockBean // EmailAuthService를 Mock 객체로 대체
    private EmailAuthService emailAuthService;

    @Test
    @Transactional // 트랜잭션 내에서 실행되도록 설정
    @Commit // 테스트 종료 후 롤백하지 않고 커밋
    public void insertDummyMembers() {
        // Mock 객체의 행동 정의:
        // emailAuthService.verifyAuthCode(어떤 이메일이든, 어떤 인증코드든)가 호출되면 항상 true를 반환하도록 설정
        when(emailAuthService.verifyAuthCode(anyString(), anyString())).thenReturn(true);

        // Optional: removeAuthInfo 호출 시 아무것도 하지 않도록 설정 (Void 메서드)
        // doNothing().when(emailAuthService).removeAuthInfo(anyString());


        for (int i = 1; i <= 10; i++) {
            MemberSignupDTO dto = MemberSignupDTO.builder()
                    .username("username" + i)
                    .password("password" + i)
                    .confirmPassword("password" + i)
                    .phoneNumber("010-1234-56" + i)
                    .name("더미사용자" + i)
                    .gender(i % 2 == 0 ? "남자" : "여자")
                    .birthDate(LocalDate.of(1990 + i % 5, i % 12 + 1, i % 28 + 1))
                    .email("dummy" + i + "@test.com")
                    .nickname("더미닉네임" + i)
                    .emailAuthCode("123456") // 이 값은 이제 실제 검증에 사용되지 않으므로 아무 값이나 넣어도 무방.
                    .build();

            try {
                Long id = memberService.registerMember(dto, null); // 프로필 없이 가입
                log.info("더미 회원 저장 성공! ID: {}", id);
            } catch (Exception e) {
                log.error("더미 회원 저장 실패: {}", e.getMessage(), e); // 스택 트레이스를 출력하여 다른 문제 발생 시 확인
            }
        }
    }
}