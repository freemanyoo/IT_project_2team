package webproject_2team.lunch_matching.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import webproject_2team.lunch_matching.domain.Board;
import webproject_2team.lunch_matching.dto.PageRequestDTO;
import webproject_2team.lunch_matching.dto.PageResponseDTO;
import webproject_2team.lunch_matching.repository.BoardRepository;
import webproject_2team.lunch_matching.service.BoardService;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardRepository boardRepository;
    private final BoardService boardService;

    @GetMapping("/board/list")
    public String list(PageRequestDTO pageRequestDTO, Model model) {
        PageResponseDTO<Board> responseDTO = boardService.getBoardList(pageRequestDTO);
        model.addAttribute("responseDTO", responseDTO);
        return "board_list";
    }

    @GetMapping("/board/read")
    public String read(@RequestParam("id") Long id, Model model) {
        try {
            Optional<Board> boardOptional = boardRepository.findById(id);
            if (boardOptional.isPresent()) {
                Board board = boardOptional.get();
                model.addAttribute("board", board);
                return "read";
            } else {
                // 게시글이 없을 경우 목록으로 리다이렉트
                return "redirect:/board/list";
            }
        } catch (Exception e) {
            // 에러 발생 시 목록으로 리다이렉트
            return "redirect:/board/list";
        }
    }

    @GetMapping("/board/register")
    public String registerForm() {
        return "board_register";
    }

    @PostMapping("/board/register")
    public String register(Board board,
                           @RequestParam("uploadImage") MultipartFile file,
                           Model model) throws IOException {

        // 제목 글자수 검증 (100바이트)
        if (board.getTitle() != null && board.getTitle().getBytes("UTF-8").length > 100) {
            model.addAttribute("titleError", "제목은 100바이트를 초과할 수 없습니다.");
            return "board_register";
        }

        // 본문 글자수 검증 (2000바이트)
        if (board.getContent() != null && board.getContent().getBytes("UTF-8").length > 2000) {
            model.addAttribute("contentError", "본문은 2000바이트를 초과할 수 없습니다.");
            return "board_register";
        }

        // 필수 필드 검증
        if (board.getTitle() == null || board.getTitle().trim().isEmpty()) {
            model.addAttribute("titleError", "제목을 입력해주세요.");
            return "board_register";
        }

        if (board.getContent() == null || board.getContent().trim().isEmpty()) {
            model.addAttribute("contentError", "본문을 입력해주세요.");
            return "board_register";
        }

        if (board.getWriter() == null || board.getWriter().trim().isEmpty()) {
            model.addAttribute("writerError", "작성자를 입력해주세요.");
            return "board_register";
        }

        if (board.getRegion() == null || board.getRegion().trim().isEmpty()) {
            model.addAttribute("regionError", "지역을 입력해주세요.");
            return "board_register";
        }

        // 이미지 처리
        if (!file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String uploadPath = "src/main/resources/static/uploads/";

            // 디렉토리가 없으면 생성
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            file.transferTo(new File(uploadPath + fileName));
            board.setImagePath("/uploads/" + fileName);
        }

        board.setCreatedAt(LocalDateTime.now());
        boardRepository.save(board);
        return "redirect:/board/list";
    }
}