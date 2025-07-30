package webproject_2team.lunch_matching.controller;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MoveController {

    @GetMapping("/test")
    public String lunch() {
        return "lunch/list"; // 오점머 화면
    }

    @GetMapping("/board/list")
    public String boardList() {
        return "board/list"; // 맛슐랭 원정대
    }

    @GetMapping("/review/list")
    public String reviewList() {
        return "review/list"; // 식후감
    }

    @GetMapping("/lunchmatch/list")
    public String bookmarkList() {
        return "lunchmatch/list"; // 맛집 저장
    }

    @GetMapping("/lunchmatch/map")
    public String mapView() {
        return "lunchmatch/map"; // 맛지도
    }

//    @PreAuthorize("isAuthenticated()")
//    @GetMapping("/mypage")
//    public String myPage() {
//        return "user/mypage"; // MY PAGE (로그인 필수)
//    }

//    @GetMapping("/help")
//    public String helpPage() {
//        return "help/index"; // 고객센터
//    }
}
