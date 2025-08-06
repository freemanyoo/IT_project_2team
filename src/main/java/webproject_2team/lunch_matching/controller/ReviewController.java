package webproject_2team.lunch_matching.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import webproject_2team.lunch_matching.dto.ReviewPageRequestDTO;
import webproject_2team.lunch_matching.dto.ReviewPageResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // Added for @AuthenticationPrincipal
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import webproject_2team.lunch_matching.security.dto.CustomUserDetails; // CustomUserDetails import 추가
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import webproject_2team.lunch_matching.dto.ReviewDTO;
import webproject_2team.lunch_matching.dto.UploadResultDTO;
import webproject_2team.lunch_matching.service.ReviewService;
import webproject_2team.lunch_matching.service.ReviewLikeService;
import webproject_2team.lunch_matching.service.ReviewCommentService;
import webproject_2team.lunch_matching.util.UploadUtil;
import webproject_2team.lunch_matching.repository.MemberRepository; // MemberRepository import 추가

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/review")
@Log4j2
@RequiredArgsConstructor

public class ReviewController {

    private final ReviewService reviewService;
    private final UploadUtil uploadUtil;
    private final ReviewLikeService reviewLikeService;
    private final ReviewCommentService reviewCommentService;
    private final MemberRepository memberRepository; // MemberRepository 주입

    // 감정 목록 정의
    private final Map<String, String> emotionMap = new LinkedHashMap<>();
    private final Map<String, String> emoticonMap = new LinkedHashMap<>();

    {
        emotionMap.put("emotion0", "기대/설렘");
        emotionMap.put("emotion1", "궁금함");
        emotionMap.put("emotion2", "식욕");
        emotionMap.put("emotion3", "만족");
        emotionMap.put("emotion4", "기쁨/즐거움");
        emotionMap.put("emotion5", "놀람");
        emotionMap.put("emotion6", "실망");
        emotionMap.put("emotion7", "혐오/역겨움");
        emotionMap.put("emotion8", "편안함");

        emoticonMap.put("emotion0", "🤩"); // 기대/설렘
        emoticonMap.put("emotion1", "🤔"); // 궁금함
        emoticonMap.put("emotion2", "😋"); // 식욕
        emoticonMap.put("emotion3", "😊"); // 만족
        emoticonMap.put("emotion4", "😄"); // 기쁨/즐거움
        emoticonMap.put("emotion5", "😮"); // 놀람
        emoticonMap.put("emotion6", "😞"); // 실망
        emoticonMap.put("emotion7", "🤢"); // 혐오/역겨움
        emoticonMap.put("emotion8", "😌"); // 편안함
    }

    @GetMapping("/register")
    public void registerGET(Model model) { // Model 추가
        log.info("register GET...");
        model.addAttribute("emotionMap", emotionMap); // 감정 맵 추가
        model.addAttribute("emoticonMap", emoticonMap); // 이모티콘 맵 추가
    }

    @PostMapping("/register")
    public String registerPOST(@AuthenticationPrincipal CustomUserDetails customUserDetails, ReviewDTO reviewDTO, RedirectAttributes redirectAttributes) {
        log.info("register POST...");
        log.info("Review registration POST request received: {}", reviewDTO);
        log.info("Authenticated memberId for review registration: {}", customUserDetails.getUsername()); // Debug log
        
        reviewDTO.setNickname(customUserDetails.getNickname()); // Set nickname from CustomUserDetails
        if (reviewDTO.getUploadFileNames() != null) {
            log.info("ReviewDTO uploadFileNames size: " + reviewDTO.getUploadFileNames().size());
            reviewDTO.getUploadFileNames().forEach(fileDto ->
                log.info("  ReviewDTO File: uuid=" + fileDto.getUuid() + ", fileName=" + fileDto.getFileName() + ", img=" + fileDto.isImg())
            );
        } else {
            log.info("ReviewDTO uploadFileNames is null.");
        }

        Long review_id = reviewService.register(reviewDTO, customUserDetails.getUsername());
        redirectAttributes.addFlashAttribute("result", review_id);
        return "redirect:/review/list";
    }

    // 파일 업로드 처리
    @PostMapping("/upload")
    @ResponseBody
    public List<UploadResultDTO> upload(List<MultipartFile> files) {
        log.info("upload POST...");
        log.info("Received files count: " + files.size());
        files.forEach(file -> log.info("  MultipartFile: " + file.getOriginalFilename() + ", size: " + file.getSize()));

        List<UploadResultDTO> resultList = uploadUtil.uploadFiles(files).join();

        log.info("UploadUtil returned resultList count: " + resultList.size());
        resultList.forEach(dto -> log.info("  UploadResultDTO: uuid=" + dto.getUuid() + ", fileName=" + dto.getFileName() + ", img=" + dto.isImg()));

        return resultList;
    }

    @GetMapping({"/read", "/modify"})
    public void read(@AuthenticationPrincipal CustomUserDetails customUserDetails, Long review_id, ReviewPageRequestDTO reviewPageRequestDTO, Model model) {
        log.info("read or modify GET...");
        ReviewDTO reviewDTO = reviewService.readOne(review_id);
        model.addAttribute("reviewDTO", reviewDTO);
        model.addAttribute("emotionMap", emotionMap); // 감정 맵 추가
        model.addAttribute("emoticonMap", emoticonMap); // 이모티콘 맵 추가
        model.addAttribute("currentMemberId", customUserDetails.getUsername()); // 현재 로그인한 사용자 ID 추가
    }

    @PostMapping("/modify")
    public String modify(@AuthenticationPrincipal CustomUserDetails customUserDetails, ReviewPageRequestDTO reviewPageRequestDTO, ReviewDTO reviewDTO, RedirectAttributes redirectAttributes) {
        log.info("modify POST...");
        reviewService.modify(reviewDTO, customUserDetails.getUsername());
        redirectAttributes.addFlashAttribute("result", "modified");
        redirectAttributes.addAttribute("review_id", reviewDTO.getReview_id());
        redirectAttributes.addAttribute("page", reviewPageRequestDTO.getPage());
        redirectAttributes.addAttribute("size", reviewPageRequestDTO.getSize());
        if (reviewPageRequestDTO.getType() != null) {
            redirectAttributes.addAttribute("type", reviewPageRequestDTO.getType());
            redirectAttributes.addAttribute("keyword", reviewPageRequestDTO.getKeyword());
        }
        return "redirect:/review/read";
    }

    @PostMapping("/remove")
    public String remove(@AuthenticationPrincipal CustomUserDetails customUserDetails, Long review_id, ReviewPageRequestDTO reviewPageRequestDTO, RedirectAttributes redirectAttributes) {
        log.info("remove POST...");
        reviewService.remove(review_id, customUserDetails.getUsername());
        redirectAttributes.addFlashAttribute("result", "removed");
        redirectAttributes.addAttribute("page", reviewPageRequestDTO.getPage());
        redirectAttributes.addAttribute("size", reviewPageRequestDTO.getSize());
        if (reviewPageRequestDTO.getType() != null) {
            redirectAttributes.addAttribute("type", reviewPageRequestDTO.getType());
            redirectAttributes.addAttribute("keyword", reviewPageRequestDTO.getKeyword());
        }
        return "redirect:/review/list";
    }

    @GetMapping("/list")
    public void list(ReviewPageRequestDTO reviewPageRequestDTO, Model model) {
        log.info("list...");
        ReviewPageResponseDTO<ReviewDTO> responseDTO = reviewService.getList(reviewPageRequestDTO);
        model.addAttribute("responseDTO", responseDTO);
        model.addAttribute("emoticonMap", emoticonMap);

        log.info("--- Debug: responseDTO.dtoList ---");
        responseDTO.getDtoList().forEach(dto -> {
            log.info("Review ID: " + dto.getReview_id() + ", UploadFileNames: " + dto.getUploadFileNames());
            if (dto.getUploadFileNames() != null && !dto.getUploadFileNames().isEmpty()) {
                dto.getUploadFileNames().forEach(fileDto -> {
                    log.info("  File DTO: uuid=" + fileDto.getUuid() + ", fileName=" + fileDto.getFileName() + ", img=" + fileDto.isImg() + ", thumbnailLink=" + fileDto.getThumbnailLink());
                });
            }
        });
    }

    // 좋아요 추가/취소
    @PostMapping("/{reviewId}/like")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleLike(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long reviewId) {
        // TODO: 실제 사용자 ID는 Spring Security 등 인증 시스템에서 가져와야 합니다.
        String memberId = customUserDetails.getUsername(); // 실제 사용자 ID

        boolean liked = reviewLikeService.toggleLike(reviewId, memberId);
        int likeCount = reviewLikeService.getLikeCount(reviewId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("liked", liked);
        response.put("likeCount", likeCount);
        return ResponseEntity.ok(response);
    }

    // 댓글 목록 조회
    @GetMapping("/{reviewId}/comments")
    @ResponseBody
    public ResponseEntity<List<webproject_2team.lunch_matching.dto.ReviewCommentDTO>> getComments(@PathVariable Long reviewId) {
        List<webproject_2team.lunch_matching.dto.ReviewCommentDTO> comments = reviewCommentService.getCommentsOfReview(reviewId);
        return ResponseEntity.ok(comments);
    }

    // 댓글 등록
    @PostMapping("/{reviewId}/comments")
    @ResponseBody
    public ResponseEntity<Long> addComment(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long reviewId, @RequestBody webproject_2team.lunch_matching.dto.ReviewCommentDTO reviewCommentDTO) {
        log.info("Authenticated memberId for comment addition: {}", customUserDetails.getUsername()); // Debug log
        reviewCommentDTO.setReview_id(reviewId);
        
        reviewCommentDTO.setNickname(customUserDetails.getNickname()); // Set nickname from CustomUserDetails
        Long commentId = reviewCommentService.register(reviewCommentDTO, customUserDetails.getUsername());
        return ResponseEntity.ok(commentId);
    }

    // 댓글 수정
    @PutMapping("/comments/{commentId}")
    @ResponseBody
    public ResponseEntity<String> modifyComment(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long commentId, @RequestBody webproject_2team.lunch_matching.dto.ReviewCommentDTO reviewCommentDTO) {
        reviewCommentDTO.setId(commentId);
        reviewCommentService.modify(reviewCommentDTO, customUserDetails.getUsername());
        return ResponseEntity.ok("Comment modified successfully");
    }

    // 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    @ResponseBody
    public ResponseEntity<String> removeComment(@AuthenticationPrincipal CustomUserDetails customUserDetails, @PathVariable Long commentId) {
        reviewCommentService.remove(commentId, customUserDetails.getUsername());
        return ResponseEntity.ok("Comment removed successfully");
    }
}