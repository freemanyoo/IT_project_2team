package webproject_2team.lunch_matching.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import webproject_2team.lunch_matching.domain.Review;
import webproject_2team.lunch_matching.dto.ReviewPageRequestDTO;
import webproject_2team.lunch_matching.dto.ReviewPageResponseDTO;
import webproject_2team.lunch_matching.dto.ReviewDTO;
import webproject_2team.lunch_matching.repository.ReviewRepository;
import webproject_2team.lunch_matching.repository.MemberRepository; // MemberRepository import 추가
import webproject_2team.lunch_matching.util.UploadUtil;

import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.Hibernate;
import org.springframework.context.event.EventListener;
import webproject_2team.lunch_matching.event.ReviewLikeUpdateEvent;

@Service
@RequiredArgsConstructor
@Log4j2
public class ReviewServiceImpl implements ReviewService {

    private final ModelMapper modelMapper;
    private final ReviewRepository reviewRepository;
    private final UploadUtil uploadUtil;
    private final ReviewLikeService reviewLikeService;
    private final MemberRepository memberRepository; // MemberRepository 주입

    @Override
    public Long register(ReviewDTO reviewDTO) {
        log.info("register...");
        Review review = modelMapper.map(reviewDTO, Review.class);

        // ModelMapper가 이미 UploadResultDTO 리스트를 Review 엔티티의 fileList에 매핑합니다.
        // 따라서 아래의 수동 추가 로직은 제거합니다.
        // if (reviewDTO.getUploadFileNames() != null && !reviewDTO.getUploadFileNames().isEmpty()) {
        //     reviewDTO.getUploadFileNames().forEach(uploadResultDTO -> {
        //         review.getFileList().add(modelMapper.map(uploadResultDTO, webproject_2team.lunch_matching.domain.UploadResult.class));
        //     });
        // }

        Long review_id = reviewRepository.save(review).getReview_id();

        log.info("Review entity before saving: " + review);
        if (review.getFileList() != null) {
            review.getFileList().forEach(file -> log.info("  File in review entity: " + file.getFileName() + ", isImage: " + file.isImage()));
        }

        return review_id;
    }

    @Transactional // 이 어노테이션을 추가합니다.
    @Override
    public ReviewDTO readOne(Long review_id) {
        java.util.Optional<Review> result = reviewRepository.findByIdWithFiles(review_id);
        Review review = result.orElseThrow();

        // Lazy-loaded 컬렉션들을 명시적으로 초기화
        Hibernate.initialize(review.getFileList());
        Hibernate.initialize(review.getComments());
        Hibernate.initialize(review.getLikes());

        log.info("Review entity likeCount before mapping: " + review.getLikeCount());

        // Add log for fileList content
        if (review.getFileList() != null) {
            log.info("Review entity fileList size after initialization: " + review.getFileList().size());
            review.getFileList().forEach(file -> log.info("  File in review entity (after init): " + file.getFileName() + ", isImage: " + file.isImage() + ", UUID: " + file.getUuid()));
        } else {
            log.info("Review entity fileList is null after initialization.");
        }

        ReviewDTO reviewDTO = modelMapper.map(review, ReviewDTO.class);

        // Set nickname for reviewDTO
        memberRepository.findByUsername(review.getMember_id()).ifPresent(member -> reviewDTO.setNickname(member.getNickname()));

        // TODO: 실제 사용자 ID는 Spring Security 등 인증 시스템에서 가져와야 합니다.
        String memberId = "testuser"; // 임시 사용자 ID
        boolean liked = reviewLikeService.isLiked(review_id, memberId);
        reviewDTO.setLikedByCurrentUser(liked);

        return reviewDTO;
    }

    @Transactional
    @Override
    public void modify(ReviewDTO reviewDTO, String memberId) {
        java.util.Optional<Review> result = reviewRepository.findByIdWithFiles(reviewDTO.getReview_id()); // Fetch with files
        Review review = result.orElseThrow(() -> new IllegalArgumentException("Review not found"));

        // 권한 확인
        if (!review.getMember_id().equals(memberId)) {
            throw new IllegalArgumentException("You do not have permission to modify this review.");
        }

        review.change(reviewDTO.getContent(), reviewDTO.getMenu(), reviewDTO.getPlace(), reviewDTO.getRating(), reviewDTO.getEmotion());

        // --- File List Management ---
        // 기존 파일 목록을 백업하여 삭제할 파일을 식별
        List<webproject_2team.lunch_matching.domain.UploadResult> oldFiles = new java.util.ArrayList<>(review.getFileList());

        // 새로운 파일 목록으로 완전히 교체
        review.getFileList().clear(); // 기존 컬렉션 비우기
        if (reviewDTO.getUploadFileNames() != null) {
            reviewDTO.getUploadFileNames().forEach(fileDTO -> {
                review.getFileList().add(modelMapper.map(fileDTO, webproject_2team.lunch_matching.domain.UploadResult.class));
            });
        }

        // 로컬 파일 시스템에서 삭제할 파일 식별 및 삭제
        // oldFiles에 있지만 review.getFileList()에 없는 파일들을 찾아 삭제
        java.util.Set<String> newFileUuids = review.getFileList().stream()
                .map(webproject_2team.lunch_matching.domain.UploadResult::getUuid)
                .collect(Collectors.toSet());

        oldFiles.forEach(oldFile -> {
            if (!newFileUuids.contains(oldFile.getUuid())) {
                String originalFileName = oldFile.getUuid() + "_" + oldFile.getFileName();
                uploadUtil.deleteFile(originalFileName); // UploadUtil의 deleteFile 호출
            }
        });
        // --- End File List Management ---

        reviewRepository.save(review);
    }

    @Transactional
    @Override
    public void remove(Long review_id, String memberId) {
        java.util.Optional<Review> result = reviewRepository.findByIdWithFiles(review_id);
        Review review = result.orElseThrow(() -> new IllegalArgumentException("Review not found"));

        // 권한 확인
        if (!review.getMember_id().equals(memberId)) {
            throw new IllegalArgumentException("You do not have permission to remove this review.");
        }

        // 로컬 파일 시스템에서 파일 삭제
        if (review.getFileList() != null && !review.getFileList().isEmpty()) {
            review.getFileList().forEach(file -> {
                String originalFileName = file.getUuid() + "_" + file.getFileName();
                uploadUtil.deleteFile(originalFileName); // UploadUtil의 deleteFile 호출
            });
        }

        reviewRepository.deleteById(review_id);
    }

    @Transactional
    @Override
    public ReviewPageResponseDTO<ReviewDTO> getList(ReviewPageRequestDTO reviewPageRequestDTO) {
        Page<Review> result = reviewRepository.searchAll(reviewPageRequestDTO, reviewPageRequestDTO.getPageable(Sort.by("review_id").descending()));

        List<ReviewDTO> dtoList = result.getContent().stream()
                .map(review -> {
                    // Initialize lazy-loaded collections before mapping
                    Hibernate.initialize(review.getFileList()); // Force initialization of the lazy-loaded collection
                    Hibernate.initialize(review.getComments()); // Initialize comments
                    Hibernate.initialize(review.getLikes());    // Initialize likes

                    log.info("--- Debug: Review entity fileList before mapping ---");
                    if (review.getFileList() != null) {
                        log.info("  Review ID: " + review.getReview_id() + ", FileList Size: " + review.getFileList().size());
                        review.getFileList().forEach(file -> log.info("  File: " + file.getFileName() + ", isImage: " + file.isImage()));
                    } else {
                        log.info("  Review ID: " + review.getReview_id() + ", fileList is null");
                    }

                    ReviewDTO reviewDTO = modelMapper.map(review, ReviewDTO.class);
                    if (review.getFileList() != null) {
                        reviewDTO.setUploadFileNames(review.getFileList().stream()
                                .map(file -> modelMapper.map(file, webproject_2team.lunch_matching.dto.UploadResultDTO.class))
                                .collect(Collectors.toList()));
                    }
                    // Set nickname for reviewDTO
                    memberRepository.findByUsername(review.getMember_id()).ifPresent(member -> reviewDTO.setNickname(member.getNickname()));

                    return reviewDTO;
                })
                .collect(Collectors.toList());

        log.info("PageRequestDTO size: " + reviewPageRequestDTO.getSize());
        log.info("Total elements from result: " + result.getTotalElements());

        return ReviewPageResponseDTO.<ReviewDTO>builder()
                .dtoList(dtoList)
                .totalCount((int)result.getTotalElements())
                .page(reviewPageRequestDTO.getPage()) // pageRequestDTO에서 page 가져오기
                .size(reviewPageRequestDTO.getSize()) // pageRequestDTO에서 size 가져오기
                .build();
    }

    @Transactional
    @Override
    public void updateLikeCount(Long reviewId, int likeCount) {
        reviewRepository.findById(reviewId).ifPresent(review -> {
            review.setLikeCount(likeCount);
            reviewRepository.save(review);
        });
    }

    @EventListener
    @Transactional
    public void handleReviewLikeUpdateEvent(ReviewLikeUpdateEvent event) {
        log.info("Handling ReviewLikeUpdateEvent for review ID: {} with like count: {}", event.getReviewId(), event.getLikeCount());
        reviewRepository.findById(event.getReviewId()).ifPresent(review -> {
            review.setLikeCount(event.getLikeCount());
            reviewRepository.save(review);
            log.info("Review likeCount updated in DB for review ID: {}", event.getReviewId());
        });
    }
}


