package webproject_2team.lunch_matching.controller.signup;

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
            model.addAttribute("loggedInUserRole", roleString);

        } else {
            // 비로그인 상태일 때 기본값 설정
            model.addAttribute("loggedInUserNickname", "비로그인");
            model.addAttribute("loggedInUserRole", "");
        }
    }
}