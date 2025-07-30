package webproject_2team.lunch_matching.controller;

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
    @GetMapping("/")
    public String rootPage() {
        log.info("GET / - Redirecting to login page.");
        return "redirect:/login";
    }

    // 만약 메인 페이지의 실제 URL이 /main 이라면 다음 핸들러도 추가
    @GetMapping("/main")
    public String mainPage() {
        log.info("GET /main - Displaying main page.");
        return "main"; // src/main/resources/templates/main.html을 렌더링
    }
}

