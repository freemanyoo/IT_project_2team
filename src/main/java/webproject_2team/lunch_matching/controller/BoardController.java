package webproject_2team.lunch_matching.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import webproject_2team.lunch_matching.domain.Board;
import webproject_2team.lunch_matching.repository.BoardRepository;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardRepository boardRepository;

    @GetMapping("/board/list")
    public String list(Model model) {
        List<Board> boardList = boardRepository.findAll();
        model.addAttribute("boardList", boardList);
        return "board_list";
    }

    @GetMapping("/board/register")
    public String registerForm() {
        return "board_register";
    }

    @PostMapping("/board/register")
    public String register(Board board,
                           @RequestParam("uploadImage") MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String uploadPath = "src/main/resources/static/uploads/";
            file.transferTo(new File(uploadPath + fileName));
            board.setImagePath("/uploads/" + fileName);
        }

        board.setCreatedAt(LocalDateTime.now());
        boardRepository.save(board);
        return "redirect:/board/list";
    }
}
