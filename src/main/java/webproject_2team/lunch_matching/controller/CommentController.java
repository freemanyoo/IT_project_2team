package webproject_2team.lunch_matching.controller;

import webproject_2team.lunch_matching.domain.CommentVO;
import webproject_2team.lunch_matching.domain.PartyBoardVO;
import webproject_2team.lunch_matching.mapper.CommentMapper;
import webproject_2team.lunch_matching.mapper.PartyBoardMapper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/comment")
@RequiredArgsConstructor
@Slf4j
@Log4j2
public class CommentController {


    private final CommentMapper commentMapper;
    private final PartyBoardMapper partyBoardMapper;

    // ✅ 댓글 등록 처리
    @PostMapping("/add")
    public String addComment(@ModelAttribute CommentVO commentVO,
                             HttpSession session,
                             RedirectAttributes rttr) {

        System.out.println("🟢 댓글 등록 컨트롤러 진입");

        // 로그인 유저 정보 세션에서 가져오기
        String writerId = (String) session.getAttribute("userId");
        String gender = (String) session.getAttribute("gender");

        System.out.println("세션에서 가져온 userId: " + writerId);
        System.out.println("세션에서 가져온 gender: " + gender);

        // 테스트용 기본값 (실제 로그인 연동 시 제거)
        if (writerId == null) {
            writerId = "testUser";
            System.out.println("writerId 강제 설정: testUser");
        }

        if (gender == null) {
            gender = "FEMALE";
            System.out.println("gender 강제 설정: FEMALE");
        }

        // 성별 제한 확인
        PartyBoardVO party = partyBoardMapper.selectOne(commentVO.getPartyId());
        String genderLimit = party.getGenderLimit();

        if (!"ALL".equalsIgnoreCase(genderLimit) && !genderLimit.equalsIgnoreCase(gender)) {
            System.out.println("🚫 성별 제한으로 댓글 등록 불가");
            rttr.addFlashAttribute("msg", "댓글 작성 권한이 없습니다.");
            rttr.addAttribute("id", commentVO.getPartyId());
            return "redirect:/party/read";
        }

        // CommentVO에 로그인 정보 세팅
        commentVO.setWriterId(writerId);
        commentVO.setGender(gender);

        System.out.println("최종 commentVO: " + commentVO);

        // 댓글 저장
        commentMapper.insertComment(commentVO);

        // 리디렉션
        rttr.addAttribute("id", commentVO.getPartyId());
        return "redirect:/party/read";
    }

    // ✅ 댓글 목록 가져오기 (선택적으로 사용할 수 있음)
    @GetMapping("/list")
    @ResponseBody
    public List<CommentVO> getCommentList(@RequestParam("partyId") Long partyId) {
        return commentMapper.getCommentsByPartyId(partyId);
    }

    // 파티 삭제 전에 사용 (비노출)
    @PostMapping("/delete-by-party")
    @ResponseBody
    public String deleteByParty(@RequestParam("partyId") Long partyId) {
        commentMapper.deleteByPartyId(partyId);
        return "success";
    }


    // 댓글 단일 삭제
    @PostMapping("/delete")
    public String deleteComment(@RequestParam("id") Long id,
                                @RequestParam("partyId") Long partyId) {
        commentMapper.deleteById(id);
        return "redirect:/party/read?id=" + partyId;
    }


    // 댓글 수정
    @PostMapping("/update")
    public String updateComment(@ModelAttribute CommentVO commentVO) {
        commentMapper.updateComment(commentVO);
        return "redirect:/party/read?id=" + commentVO.getPartyId();
    }


}
