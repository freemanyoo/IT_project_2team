package webproject_2team.lunch_matching.controller;

import  webproject_2team.lunch_matching.dto.LunchMatchDTO;
import  webproject_2team.lunch_matching.service.LunchMatchService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/lunchmatch")
@RequiredArgsConstructor
@Log4j2
public class LunchMatchViewController {

    private final LunchMatchService lunchMatchService;

    @GetMapping("/list")
    public String listPage( // 이 메서드는 이제 뷰(HTML)만 반환
                            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
                            @RequestParam(value = "category", required = false, defaultValue = "") String category,
                            @RequestParam(value = "orderBy", required = false, defaultValue = "latest") String orderBy,
                            @RequestParam(value = "minRating", required = false) Double minRating,
                            Model model) {

        log.info("저장된 맛집 목록 페이지 요청 (뷰 렌더링용): keyword={}, category={}, orderBy={}, minRating={}", keyword, category, orderBy, minRating);
        // model.addAttribute("lunchMatches", lunchMatches); // 데이터를 뷰로 직접 전달하지 않음
        // 검색 파라미터를 뷰에 전달하여 폼의 기존 값 유지
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("orderBy", orderBy);
        model.addAttribute("minRating", minRating);
        return "lunchmatch/list"; // list.html 뷰 반환
    }

    // --- 추가된 부분: 맛집 목록 데이터를 JSON으로 반환하는 API 엔드포인트 ---
    @GetMapping("/api/list")
    @ResponseBody // 이 메서드는 JSON 데이터를 직접 응답합니다.
    public List<LunchMatchDTO> getLunchMatchesJson(
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "category", required = false, defaultValue = "") String category,
            @RequestParam(value = "orderBy", required = false, defaultValue = "latest") String orderBy,
            @RequestParam(value = "minRating", required = false) Double minRating) {

        log.info("맛집 목록 데이터 요청 (AJAX API용): keyword={}, category={}, orderBy={}, minRating={}", keyword, category, orderBy, minRating);
        // 서비스에서 데이터를 조회하여 JSON으로 반환
        return lunchMatchService.searchAndSort(keyword, category, minRating, orderBy);
    }
    // -------------------------------------------------------------------

    @GetMapping("/read/{rno}")
    public String read(@PathVariable("rno") Long rno, Model model) {
        log.info("맛집 상세 정보 페이지 요청: rno = " + rno);
        LunchMatchDTO lunchMatchDTO = lunchMatchService.getOne(rno);
        model.addAttribute("dto", lunchMatchDTO);
        return "lunchmatch/read";
    }

    @GetMapping("/modify/{rno}")
    public String modifyForm(@PathVariable("rno") Long rno, Model model) {
        log.info("맛집 수정 페이지 요청: rno = " + rno);
        LunchMatchDTO lunchMatchDTO = lunchMatchService.getOne(rno);
        model.addAttribute("dto", lunchMatchDTO);
        return "lunchmatch/modify";
    }

    @PostMapping("/modify")
    public String modify(
            @Valid LunchMatchDTO lunchMatchDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        log.info("맛집 수정 처리 요청: " + lunchMatchDTO);

        if (bindingResult.hasErrors()) {
            log.error("수정 데이터 유효성 검사 오류: " + bindingResult.getAllErrors());
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/lunchmatch/modify/" + lunchMatchDTO.getRno();
        }

        lunchMatchService.modify(lunchMatchDTO);

        return "redirect:/lunchmatch/read/" + lunchMatchDTO.getRno();
    }

    @GetMapping("/map")
    public String mapPage(Model model) {
        log.info("Request for lunchmatch map page...");
        return "lunchmatch/map";
    }
}