package webproject_2team.lunch_matching.controller.signup;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import webproject_2team.lunch_matching.security.dto.CustomUserDetails;

// layout.html에서 닉네임 + 역할 나오게 하기위해서 만듦
@ControllerAdvice
public class CommonControllerAdvice {
    // 모든 요청에 대해 CustomUserDetails를 Model에 추가
    @ModelAttribute
    public void addCurrentUserDetails(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        if (userDetails != null) {
            model.addAttribute("loggedInUserNickname", userDetails.getNickname());

            String roleString;
            // ADMIN 역할을 우선하여 찾음
            if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                roleString = "관리자";
            } else if (userDetails.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER"))) {
                roleString = "일반유저";
            } else {
                roleString = "UNKNOWN"; // 알 수 없는 역할
            }
            model.addAttribute("loggedInUserRole", roleString != null ? roleString : "");

        } else {
            // 비로그인 상태일 때 기본값 설정
            model.addAttribute("loggedInUserNickname", "비로그인");
            model.addAttribute("loggedInUserRole", "");
        }
    }

    @ModelAttribute("isAuthenticated")
    public boolean isAuthenticated() {
        // 수정 원인: layout.html에서 sec:authorize 대신 th:if로 로그인 상태를 체크하려고 추가함.
        // 기능: 현재 사용자가 로그인했는지 여부를 뷰에 알려줌.
        // 리뷰 등록에 미친 영향: 로그인한 사용자만 리뷰를 등록할 수 있도록 폼의 버튼을 제어하거나, UI를 조건부로 보여줄 수 있게 됨.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && !("anonymousUser".equals(authentication.getPrincipal()));
    }

    @ModelAttribute("currentUsername")
    public String currentUsername() {
        // 수정 원인: layout.html에서 로그인한 사용자 이름을 표시하려고 추가함.
        // 기능: 현재 로그인한 사용자의 이름을 뷰에 전달함.
        // 리뷰 등록에 미친 영향: 리뷰 등록 폼의 작성자 ID 필드에 로그인한 사용자 ID를 자동으로 채워줄 수 있게 됨.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !("anonymousUser".equals(authentication.getPrincipal()))) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                return ((UserDetails) principal).getUsername();
            } else {
                return principal.toString();
            }
        }
        return null;
    }
}