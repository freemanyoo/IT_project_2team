package webproject_2team.lunch_matching.controller;



import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * 게시글 관련 요청을 처리하는 컨트롤러 클래스입니다.
 */
@Controller
@RequiredArgsConstructor
public class BoardController {

    /**
     * 게시글 목록 페이지를 반환합니다.
     * @return 게시글 목록 뷰 이름
     */
    @GetMapping("/board/list")
    public String listPage() {
        return "board/list";
    }
}