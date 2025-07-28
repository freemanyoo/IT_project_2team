package webproject_2team.lunch_matching.controller;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import webproject_2team.lunch_matching.domain.CommentEntity;
import webproject_2team.lunch_matching.domain.PartyBoardEntity;
import webproject_2team.lunch_matching.dto.PartyPageRequestDTO;
import webproject_2team.lunch_matching.dto.PartyPageResponseDTO;
import webproject_2team.lunch_matching.repository.CommentRepository;
import webproject_2team.lunch_matching.service.PartyBoardService;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/party")
public class PartyBoardController {

    private final PartyBoardService partyBoardService;
    private final CommentRepository commentRepository;

    @GetMapping("/write")
    public String writeForm() {
        return "party/party_write";
    }

    @PostMapping("/write")
    public String writeSubmit(PartyBoardEntity entity) {
        entity.setWriterId(1L); // 임시 사용자 ID
        entity.setLatitude(35.1796);
        entity.setLongitude(129.0756);
        partyBoardService.register(entity);
        return "redirect:/party/list";
    }

    @GetMapping("/list")
    public String list(@ModelAttribute PartyPageRequestDTO requestDTO, Model model) {
        PartyPageResponseDTO<PartyBoardEntity> response = partyBoardService.getList(requestDTO);
        model.addAttribute("response", response);
        model.addAttribute("requestDTO", requestDTO);
        return "party/list";
    }

    @GetMapping("/read")
    public String read(@RequestParam("id") Long id, RedirectAttributes rttr, Model model) {
        PartyBoardEntity entity = partyBoardService.get(id);

        if (entity.getDeadline() != null && entity.getDeadline().isBefore(LocalDateTime.now())) {
            rttr.addFlashAttribute("msg", "마감된 모집입니다.");
            return "redirect:/party/list";
        }

        model.addAttribute("party", entity);
        List<CommentEntity> commentList = commentRepository.findByPartyIdOrderByCreatedAtAsc(id);
        model.addAttribute("commentList", commentList);
        return "party/party_read";
    }

    @PostMapping("/delete")
    @Transactional
    public String delete(@RequestParam("id") Long id) {
        commentRepository.deleteByPartyId(id);  // ✅ 댓글 먼저 삭제
        partyBoardService.delete(id);           // ✅ 그 다음 게시글 삭제
        return "redirect:/party/list";
    }

    @GetMapping("/update")
    public String updateForm(@RequestParam("id") Long id, Model model) {
        PartyBoardEntity entity = partyBoardService.get(id);
        model.addAttribute("party", entity);
        return "party/party_update";
    }

    @PostMapping("/update")
    public String updateSubmit(PartyBoardEntity updatedEntity) {
        // 기존 데이터 조회
        PartyBoardEntity origin = partyBoardService.get(updatedEntity.getId());

        // ✅ writerId는 수정 안되게 기존 값 유지
        updatedEntity.setWriterId(origin.getWriterId());

        // 업데이트 수행
        partyBoardService.update(updatedEntity);

        return "redirect:/party/read?id=" + updatedEntity.getId();
    }


}
