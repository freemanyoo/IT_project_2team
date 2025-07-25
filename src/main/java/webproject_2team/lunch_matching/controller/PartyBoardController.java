package webproject_2team.lunch_matching.controller;


import webproject_2team.lunch_matching.domain.CommentVO;
import webproject_2team.lunch_matching.domain.PartyBoardVO;
import webproject_2team.lunch_matching.dto.PageRequestDTO;
import webproject_2team.lunch_matching.dto.PageResponseDTO;
import webproject_2team.lunch_matching.mapper.CommentMapper;
import webproject_2team.lunch_matching.service.PartyBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/party")
public class PartyBoardController {

    private final PartyBoardService partyBoardService;
    private final CommentMapper commentMapper;

    @GetMapping("/write")
    public String writeForm() {
        return "party/party_write";
    }

    @PostMapping("/write")
    public String writeSubmit(PartyBoardVO vo) {
        vo.setWriterId(1L);
        vo.setLatitude(35.1796);
        vo.setLongitude(129.0756);
        partyBoardService.register(vo);
        return "redirect:/party/list";
    }

    @GetMapping("/list")
    public String list(@ModelAttribute PageRequestDTO requestDTO, Model model) {
        PageResponseDTO<PartyBoardVO> response = partyBoardService.getList(requestDTO);
        model.addAttribute("response", response);
        model.addAttribute("requestDTO", requestDTO);
        return "party/list";
    }





    @GetMapping("/read")
    public String read(@RequestParam("id") Long id, RedirectAttributes rttr, Model model) {
        PartyBoardVO vo = partyBoardService.get(id);

        // 마감 확인 로직
        if (vo.getDeadline() != null && vo.getDeadline().isBefore(LocalDateTime.now())) {
            rttr.addFlashAttribute("msg", "마감된 모집입니다.");
            return "redirect:/party/list";
        }

        model.addAttribute("party", vo);
        List<CommentVO> commentList = commentMapper.getCommentsByPartyId(id);
        model.addAttribute("commentList", commentList);
        return "party/party_read";
    }


    @PostMapping("/delete")
    public String delete(@RequestParam("id") Long id) {
        commentMapper.deleteByPartyId(id);  // ✅ 댓글 먼저 삭제
        partyBoardService.delete(id);       // ✅ 그 다음 게시글 삭제
        return "redirect:/party/list";
    }

    @GetMapping("/update")
    public String updateForm(@RequestParam("id") Long id, Model model) {
        PartyBoardVO vo = partyBoardService.get(id);
        model.addAttribute("party", vo);
        return "party/party_update";
    }

    @PostMapping("/update")
    public String updateSubmit(PartyBoardVO vo) {
        partyBoardService.update(vo);
        return "redirect:/party/read?id=" + vo.getId(); // 수정 완료 후 상세보기로
    }





}
