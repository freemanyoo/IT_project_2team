package webproject_2team.lunch_matching.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 에러 관련 요청을 처리하는 컨트롤러 클래스입니다.
 */
@Controller
public class ErrorController {

    /**
     * 접근 거부 페이지를 반환합니다.
     * 사용자가 정지되어 접근이 제한될 때 이 페이지로 리다이렉트됩니다.
     * @return 접근 거부 페이지의 뷰 이름
     */
    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}
