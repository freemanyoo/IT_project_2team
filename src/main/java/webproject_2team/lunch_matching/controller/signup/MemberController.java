package webproject_2team.lunch_matching.controller.signup;

import webproject_2team.lunch_matching.dto.signup.MemberResponseDTO;
import webproject_2team.lunch_matching.dto.signup.MemberSignupDTO;
import webproject_2team.lunch_matching.dto.signup.MemberUpdateDTO;
import webproject_2team.lunch_matching.dto.signup.ProfileDTO;
import webproject_2team.lunch_matching.service.signup.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User; // Spring Security User 클래스
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api/members") //기본 URL 경로를 /api/members
@RequiredArgsConstructor // final 필드에 대한 생성자 자동 생성
@Log4j2
public class MemberController { // 또는 MemberRestController

    private final MemberService memberService;
    // 회원 가입 엔드포인트
    @PostMapping(value = "/signup", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> signupMember(
            @Valid @RequestPart("memberSignupDTO") MemberSignupDTO memberSignupDTO,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        log.info("회원가입 요청 수신: {}", memberSignupDTO.getUsername());
        if (!memberSignupDTO.getPassword().equals(memberSignupDTO.getConfirmPassword())) {
            log.warn("회원가입 실패: 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
            return ResponseEntity.badRequest().body(Map.of("error", "비밀번호와 비밀번호 확인이 일치하지 않습니다."));
        }
        ProfileDTO profileDTO = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            profileDTO = ProfileDTO.builder().file(profileImage).build();
        }
        try {
            Long memberId = memberService.registerMember(memberSignupDTO, profileDTO);
            log.info("회원가입 성공, Member ID: {}", memberId);
            return ResponseEntity.ok(Map.of("message", "회원가입이 성공적으로 완료되었습니다.", "memberId", memberId.toString()));
        } catch (IllegalArgumentException e) {
            log.warn("회원가입 실패 (유효성 검사 또는 중복): {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("회원가입 중 서버 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "회원가입 중 서버 오류가 발생했습니다."));
        }
    }

    // --- 회원 정보 조회 엔드포인트 (username 기준) ---
    // modify.html에서 회원 정보를 불러올 때 이 GET 요청을 사용합니다.
    @GetMapping("/{username}")
    public ResponseEntity<MemberResponseDTO> getMemberInfo(@PathVariable String username,
                                                           @AuthenticationPrincipal User user) { // 로그인된 사용자 정보 주입
        log.info("회원 정보 조회 요청, Username: {}", username);

        // 현재 로그인된 사용자와 조회하려는 username이 일치하는지 확인 (보안 강화)
        if (user == null || !user.getUsername().equals(username)) {
            log.warn("권한 없는 회원 정보 조회 시도: 요청 username={}, 로그인 username={}", username, user != null ? user.getUsername() : "N/A");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
        }

        try {
            MemberResponseDTO memberResponseDTO = memberService.getMemberByUsername(username);
            return ResponseEntity.ok(memberResponseDTO);
        } catch (IllegalArgumentException e) {
            log.warn("회원 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build(); // 404 Not Found
        } catch (Exception e) {
            log.error("회원 조회 중 서버 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 회원 정보 수정 엔드포인트 (username 기준, 비밀번호 및 프로필 이미지 포함)
    @PutMapping(value = "/{username}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> updateMember(
            @PathVariable String username,
            @Valid @RequestPart("memberUpdateDTO") MemberUpdateDTO memberUpdateDTO,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage,
            @AuthenticationPrincipal User user) { // 로그인된 사용자 정보 주입

        log.info("회원 정보 수정 요청, Username: {}", username);

        // 현재 로그인된 사용자와 수정하려는 username이 일치하는지 확인 (보안 강화)
        if (user == null || !user.getUsername().equals(username)) {
            log.warn("권한 없는 회원 정보 수정 시도: 요청 username={}, 로그인 username={}", username, user != null ? user.getUsername() : "N/A");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "권한이 없습니다."));
        }

        ProfileDTO profileDTO = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            profileDTO = ProfileDTO.builder().file(profileImage).build();
        }

        try {
            memberService.updateMember(username, memberUpdateDTO, profileDTO);
            log.info("회원 정보 수정 성공, Username: {}", username);
            return ResponseEntity.ok(Map.of("message", "회원 정보가 성공적으로 수정되었습니다."));
        } catch (IllegalArgumentException e) {
            log.warn("회원 정보 수정 실패 (유효성 검사 또는 중복): {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            log.error("회원 정보 수정 중 서버 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "회원 정보 수정 중 오류가 발생했습니다."));
        }
    }

    // 회원 삭제 엔드포인트 (username 기준)
    @DeleteMapping("/{username}")
    public ResponseEntity<Map<String, String>> deleteMember(@PathVariable String username,
                                                            @AuthenticationPrincipal User user,
                                                            HttpServletRequest request) { // HttpServletRequest 파라미터 추가
        log.info("회원 삭제 요청, Username: {}", username);

        // 현재 로그인된 사용자와 삭제하려는 username이 일치하는지 확인 (보안 강화)
        if (user == null || !user.getUsername().equals(username)) {
            log.warn("권한 없는 회원 삭제 시도: 요청 username={}, 로그인 username={}", username, user != null ? user.getUsername() : "N/A");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "권한이 없습니다."));
        }

        try {
            memberService.deleteMember(username);
            log.info("회원 삭제 성공, Username: {}", username);

            // 회원 삭제 성공 후 로그아웃 처리
            // 세션을 무효화하여 현재 사용자의 인증 상태를 제거합니다.
            request.getSession().invalidate();

            return ResponseEntity.ok(Map.of("message", "회원이 성공적으로 삭제되었습니다."));
        } catch (IllegalArgumentException e) {
            log.warn("회원 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("회원 삭제 중 서버 오류: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "회원 삭제 중 오류가 발생했습니다."));
        }
    }
}