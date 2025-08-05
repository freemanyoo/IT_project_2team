package webproject_2team.lunch_matching.config.signup;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import webproject_2team.lunch_matching.domain.signup.Member;
import webproject_2team.lunch_matching.domain.signup.MemberRole;
import webproject_2team.lunch_matching.repository.MemberRepository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set; // Set import 추가
@Component
@RequiredArgsConstructor
@Log4j2
public class AdminAccountInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
// ADMIN 계정의 존재 여부 확인 (예: 특정 username으로)
        String adminUsername = "admin"; // 관리자 계정 아이디
        Optional<Member> adminMemberOptional = memberRepository.findByUsername(adminUsername);

        if (adminMemberOptional.isEmpty()) {
            log.info("관리자 계정이 존재하지 않습니다. 새로 생성합니다.");

            // ADMIN 계정 정보 설정
            Member admin = Member.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode("12341234"))
                    .phoneNumber("010-9999-8888")
                    .name("관리자")
                    .gender("male") // 성별은 아무거나 설정 가능
                    .birthDate(LocalDate.of(1990, 1, 1))
                    .email("admin@lunchmatch.com") // 관리자 이메일
                    .nickname("관리자")
                    .roles(Set.of(MemberRole.USER, MemberRole.ADMIN)) // USER와 ADMIN 역할 모두 부여
                    .hasProfileImage(false) // 프로필 이미지 없음
                    .build();
            memberRepository.save(admin);
            log.info("관리자 계정 '{}'이 성공적으로 생성되었습니다.", adminUsername);
        } else {
            log.info("관리자 계정 '{}'이 이미 존재합니다.", adminUsername);
            // 혹시 기존 ADMIN 계정에 ADMIN 역할이 없는 경우 추가 (선택 사항)
            Member existingAdmin = adminMemberOptional.get();
            if (!existingAdmin.getRoles().contains(MemberRole.ADMIN)) {
                existingAdmin.addRole(MemberRole.ADMIN);
                memberRepository.save(existingAdmin);
                log.info("기존 관리자 계정 '{}'에 ADMIN 역할이 추가되었습니다.", adminUsername);
            }
        }
    }
}
