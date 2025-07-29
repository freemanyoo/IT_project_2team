package webproject_2team.lunch_matching.controller;

import webproject_2team.lunch_matching.dto.PageRequestDTO;
import webproject_2team.lunch_matching.dto.PageResponseDTO;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import webproject_2team.lunch_matching.dto.ReviewDTO;
import webproject_2team.lunch_matching.dto.UploadResultDTO;
import webproject_2team.lunch_matching.service.ReviewService;
import webproject_2team.lunch_matching.util.UploadUtil;

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
    public String registerPOST(ReviewDTO reviewDTO, RedirectAttributes redirectAttributes) {
        log.info("register POST...");
        Long review_id = reviewService.register(reviewDTO);
        redirectAttributes.addFlashAttribute("result", review_id);
        return "redirect:/review/list";
    }

    // 파일 업로드 처리
    @PostMapping("/upload")
    @ResponseBody
    public List<UploadResultDTO> upload(List<MultipartFile> files) {
        log.info("upload POST...");
        return uploadUtil.uploadFiles(files).join();
    }

    @GetMapping({"/read", "/modify"})
    public void read(Long review_id, PageRequestDTO pageRequestDTO, Model model) {
        log.info("read or modify GET...");
        ReviewDTO reviewDTO = reviewService.readOne(review_id);
        model.addAttribute("reviewDTO", reviewDTO);
        model.addAttribute("emotionMap", emotionMap); // 감정 맵 추가
        model.addAttribute("emoticonMap", emoticonMap); // 이모티콘 맵 추가
    }

    @PostMapping("/modify")
    public String modify(PageRequestDTO pageRequestDTO, ReviewDTO reviewDTO, RedirectAttributes redirectAttributes) {
        log.info("modify POST...");
        reviewService.modify(reviewDTO);
        redirectAttributes.addFlashAttribute("result", "modified");
        redirectAttributes.addAttribute("review_id", reviewDTO.getReview_id());
        redirectAttributes.addAttribute("page", pageRequestDTO.getPage());
        redirectAttributes.addAttribute("size", pageRequestDTO.getSize());
        if (pageRequestDTO.getType() != null) {
            redirectAttributes.addAttribute("type", pageRequestDTO.getType());
            redirectAttributes.addAttribute("keyword", pageRequestDTO.getKeyword());
        }
        return "redirect:/review/read";
    }

    @PostMapping("/remove")
    public String remove(Long review_id, PageRequestDTO pageRequestDTO, RedirectAttributes redirectAttributes) {
        log.info("remove POST...");
        reviewService.remove(review_id);
        redirectAttributes.addFlashAttribute("result", "removed");
        redirectAttributes.addAttribute("page", pageRequestDTO.getPage());
        redirectAttributes.addAttribute("size", pageRequestDTO.getSize());
        if (pageRequestDTO.getType() != null) {
            redirectAttributes.addAttribute("type", pageRequestDTO.getType());
            redirectAttributes.addAttribute("keyword", pageRequestDTO.getKeyword());
        }
        return "redirect:/review/list";
    }

    @GetMapping("/list")
    public void list(PageRequestDTO pageRequestDTO, Model model) {
        log.info("list...");
        PageResponseDTO<ReviewDTO> responseDTO = reviewService.getList(pageRequestDTO);
        model.addAttribute("responseDTO", responseDTO);
        model.addAttribute("emotionMap", emotionMap);
        model.addAttribute("emoticonMap", emoticonMap); // 이모티콘 맵 추가 // 감정 맵 추가

        // 각 ReviewDTO의 uploadFileNames 확인 로그 추가
        responseDTO.getDtoList().forEach(reviewDTO -> {
            if (reviewDTO.getUploadFileNames() != null && !reviewDTO.getUploadFileNames().isEmpty()) {
                log.info("Review ID: " + reviewDTO.getReview_id() + ", Files: " + reviewDTO.getUploadFileNames().size());
                reviewDTO.getUploadFileNames().forEach(file -> log.info("  File: " + file.getFileName() + ", Link: " + file.getLink()));
            } else {
                log.info("Review ID: " + reviewDTO.getReview_id() + ", No files.");
            }
        });
    }
}