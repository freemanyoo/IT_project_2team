package webproject_2team.lunch_matching.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import webproject_2team.lunch_matching.domain.Board;
import webproject_2team.lunch_matching.domain.Comment;
import webproject_2team.lunch_matching.dto.PageRequestDTO;
import webproject_2team.lunch_matching.dto.PageResponseDTO;
import webproject_2team.lunch_matching.security.dto.CustomUserDetails;
import webproject_2team.lunch_matching.service.BoardService;
import webproject_2team.lunch_matching.service.CommentService;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;

    @Value("${kakao.javascript.api.key}")
    private String kakaoJavascriptApiKey;

    // =================================================================
    // 1. 페이지 조회 (GET Mappings)
    // =================================================================

    /**
     * 게시글 목록 페이지
     */
    @GetMapping("/board/list")
    public String list(PageRequestDTO pageRequestDTO, Model model) {
        PageResponseDTO<Board> responseDTO = boardService.getBoardList(pageRequestDTO);
        model.addAttribute("responseDTO", responseDTO);
        model.addAttribute("pageRequestDTO", pageRequestDTO);
        return "board_list";
    }

    /**
     * 게시글 상세 조회 페이지
     */
    @GetMapping("/board/read")
    public String read(@RequestParam("id") Long id,
                       PageRequestDTO pageRequestDTO, // page, type, keyword 등을 이 객체로 한번에 받습니다.
                       Model model,
                       @AuthenticationPrincipal CustomUserDetails userDetails,
                       RedirectAttributes redirectAttributes) {

        Board board = boardService.read(id);
        if (board == null) {
            return "redirect:/board/list";
        }

        // --- 접근 권한 검사 시작 ---
        boolean canAccess = false;

// 1. 본인 글인지 확인
        if (userDetails != null && userDetails.getEmail().equals(board.getWriterEmail())) {
            canAccess = true;
        }

// 2. 성별 제한이 없는 글인지 확인
        if (!canAccess && "성별상관무".equals(board.getGenderLimit())) {
            canAccess = true;
        }

// 3. 성별 제한이 있는 글일 경우 (수정된 로직)
        if (!canAccess && userDetails != null) {
            String userGender = userDetails.getGender(); // 예: "female"
            String boardLimit = board.getGenderLimit();  // 예: "여"

            // <<--- 값 변환 로직 추가 ---
            if ("female".equalsIgnoreCase(userGender)) {
                userGender = "여";
            } else if ("male".equalsIgnoreCase(userGender)) {
                userGender = "남";
            }
            // --- 값 변환 로직 끝 --- >>

            // 변환된 값으로 비교
            if (userGender != null && userGender.equals(boardLimit)) {
                canAccess = true;
            }
        }

// 4. 최종 접근 거부 처리
        if (!canAccess) {
            redirectAttributes.addFlashAttribute("error_message", "이 게시글에 접근할 권한이 없습니다.");
            return "redirect:/board/list" + pageRequestDTO.getLink();
        }
// --- 접근 권한 검사 끝 ---

        // --- 모델에 데이터 추가 ---
        model.addAttribute("board", board);
        model.addAttribute("comments", commentService.getCommentsByBoardId(id));
        model.addAttribute("kakaoJsApiKey", kakaoJavascriptApiKey);
        // DTO 객체 전체를 모델에 추가합니다.
        model.addAttribute("pageRequestDTO", pageRequestDTO);

        if (userDetails != null) {
            model.addAttribute("loggedInNickname", userDetails.getNickname());
            model.addAttribute("loggedInUserEmail", userDetails.getEmail());
        }

        return "read";
    }

    /**
     * 게시글 작성 페이지
     */
    @GetMapping("/board/register")
    public String registerForm(Model model, @AuthenticationPrincipal CustomUserDetails userDetails) { // <-- 1. 로그인 정보 받기
        model.addAttribute("kakaoJsApiKey", kakaoJavascriptApiKey);
        model.addAttribute("board", new Board());

        // ===== 로그인 사용자 정보 처리 시작 =====
        // 2. 로그인한 사용자의 닉네임을 모델에 담아 HTML로 전달
        //    (작성자 입력란에 자동으로 채워넣기 위함)
        if (userDetails != null) {
            model.addAttribute("loggedInNickname", userDetails.getNickname());
        }
        // ===== 로그인 사용자 정보 처리 끝 =====
        return "board_register";
    }

    /**
     * 게시글 수정 페이지
     */
    @GetMapping("/board/modify/{id}")
    public String modifyForm(@PathVariable("id") Long id, Model model) {
        // 수정 페이지는 기존 데이터를 불러오므로, 현재 로그인 정보는 필요하지 않습니다.
        Board board = boardService.read(id);
        model.addAttribute("board", board);
        model.addAttribute("kakaoJsApiKey", kakaoJavascriptApiKey);
        return "board_modify";
    }

    // =================================================================
    // 2. 폼 제출 처리 (POST Mappings)
    // =================================================================

    /**
     * 게시글 등록 처리
     */
    @PostMapping("/board/register")
    public String register(@ModelAttribute Board board, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) throws IOException {
        // ===== 로그인 사용자 처리 시작 =====
        if (userDetails == null) {
            return "redirect:/login"; // 1. 비로그인 시 로그인 페이지로
        }
        // 2. Board 객체에 작성자 닉네임과 이메일 설정
        board.setWriter(userDetails.getNickname());
        board.setWriterEmail(userDetails.getEmail());
        // ===== 로그인 사용자 처리 끝 =====

        // ... 기존 유효성 검사 (작성자 필드 검사는 제거) ...

        board.setCreatedAt(LocalDateTime.now());
        boardService.save(board);
        return "redirect:/board/list";
    }

    /**
     * 게시글 수정 처리
     */
    @PostMapping("/board/modify")
    public String modify(@ModelAttribute Board board, @AuthenticationPrincipal CustomUserDetails userDetails, Model model) throws IOException {
        if (userDetails == null) {
            return "redirect:/login";
        }
        try {
            // ... 기존 유효성 검사 ...
            // 서비스 호출 시, 권한 확인을 위해 현재 로그인한 사용자의 이메일 전달
            boardService.modify(board, userDetails.getEmail());
        } catch (AccessDeniedException e) {
            // 서비스에서 권한 없음 예외 발생 시 에러 페이지로 이동
            return "redirect:/error/access-denied"; // (에러 페이지는 별도 구현 필요)
        }
        return "redirect:/board/read?id=" + board.getId();
    }

    /**
     * 게시글 삭제 처리
     */
    @PostMapping("/board/delete/{id}")
    public String delete(@PathVariable("id") Long id, @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        try {
            // 서비스 호출 시, 권한 확인을 위해 현재 로그인한 사용자의 이메일 전달
            boardService.delete(id, userDetails.getEmail());
        } catch (AccessDeniedException e) {
            return "redirect:/error/access-denied";
        }
        return "redirect:/board/list";
    }

    // =================================================================
    // 3. REST API (댓글 처리)
    // =================================================================

    /**
     * 댓글 작성 API
     */
    @PostMapping("/api/board/comment")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addCommentApi(@RequestParam("boardId") Long boardId,
                                                             @RequestParam("content") String content,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        if (userDetails == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response); // 401: Unauthorized
        }
        try {
            // 서비스 호출 시, 닉네임과 이메일 모두 전달
            Comment comment = commentService.saveComment(boardId, content, userDetails.getNickname(), userDetails.getEmail());

            Map<String, Object> commentData = new HashMap<>();
            commentData.put("id", comment.getId());
            commentData.put("content", comment.getContent());
            commentData.put("writer", comment.getWriter());
            commentData.put("createdAt", comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
            response.put("success", true);
            response.put("comment", commentData);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 댓글 수정 API
     */
    @PutMapping("/api/board/comment/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> updateComment(@PathVariable("id") Long commentId,
                                                             @RequestBody Map<String, String> payload,
                                                             @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        if (userDetails == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }
        try {
            String content = payload.get("content");
            Comment updatedComment = commentService.updateComment(commentId, content, userDetails.getEmail());

            Map<String, Object> commentData = new HashMap<>();
            commentData.put("id", updatedComment.getId());
            commentData.put("content", updatedComment.getContent());
            response.put("success", true);
            response.put("comment", commentData);
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            response.put("success", false);
            response.put("message", "수정 권한이 없습니다.");
            return ResponseEntity.status(403).body(response); // 403: Forbidden
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 댓글 삭제 API
     */
    @DeleteMapping("/api/board/comment/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCommentApi(@PathVariable("id") Long commentId,
                                                                @AuthenticationPrincipal CustomUserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        if (userDetails == null) {
            response.put("success", false);
            response.put("message", "로그인이 필요합니다.");
            return ResponseEntity.status(401).body(response);
        }
        try {
            commentService.deleteComment(commentId, userDetails.getEmail());
            response.put("success", true);
            response.put("message", "댓글이 삭제되었습니다.");
            return ResponseEntity.ok(response);
        } catch (AccessDeniedException e) {
            response.put("success", false);
            response.put("message", "삭제 권한이 없습니다.");
            return ResponseEntity.status(403).body(response); // 403: Forbidden
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
