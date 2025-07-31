package webproject_2team.lunch_matching.controller.signup;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Log4j2
public class LoginController {

    @GetMapping("/login")
    public String loginPage() {
        log.info("GET /login - Displaying login page.");
        return "login"; // src/main/resources/templates/login.html을 렌더링
    }

    // 로그인 성공 시 이동할 메인 페이지 또는 루트 페이지 핸들러
    // SecurityConfig.defaultSuccessUrl("/") 와 일치해야 합니다.
//    @GetMapping("/")
//    public String rootPage() {
//        log.info("GET / - Redirecting to main page (or showing index).");
//        // 실제 메인 페이지가 있다면 해당 경로로 리다이렉트하거나 해당 템플릿을 반환
//        // 예시: Thymeleaf로 만든 메인 페이지가 "main1.html"이라면
//        return "main"; // 또는 "redirect:/main"
//    }

    // 로그인 안한 사용자도 /main 을 GET방식으로 들어갈수있게
    @GetMapping("/main")
    public String mainPage() {
        log.info("GET /main - Displaying main page.");
        return "main1"; // src/main/resources/templates/main1.html을 렌더링
    }
}

