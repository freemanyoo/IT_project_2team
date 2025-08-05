package webproject_2team.lunch_matching.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import webproject_2team.lunch_matching.domain.Review;
import webproject_2team.lunch_matching.domain.ReviewComment;
import webproject_2team.lunch_matching.dto.ReviewCommentDTO;
import webproject_2team.lunch_matching.repository.ReviewCommentRepository;
import webproject_2team.lunch_matching.repository.ReviewRepository;
import webproject_2team.lunch_matching.repository.MemberRepository; // MemberRepository import 추가

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional(readOnly = true)
public class ReviewCommentServiceImpl implements ReviewCommentService {

    private final ModelMapper modelMapper;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository; // MemberRepository 주입

    @Transactional
    @Override
    public Long register(ReviewCommentDTO reviewCommentDTO, String username) {
        log.info("댓글 등록: {}", reviewCommentDTO);
        Review review = reviewRepository.findById(reviewCommentDTO.getReview_id())
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다. ID: " + reviewCommentDTO.getReview_id()));

        ReviewComment reviewComment = modelMapper.map(reviewCommentDTO, ReviewComment.class);
        reviewComment.setReview(review);

        // Member 엔티티를 조회하여 ReviewComment에 설정
        memberRepository.findByUsername(username).ifPresent(member -> {
            reviewComment.setMember(member);
        });

        return reviewCommentRepository.save(reviewComment).getId();
    }

    @Override
    public List<ReviewCommentDTO> getCommentsOfReview(Long reviewId) {
        log.info("댓글 목록 조회: reviewId={}", reviewId);
        List<ReviewComment> comments = reviewCommentRepository.findByReview_Review_idOrderByRegDateAsc(reviewId);

        log.info("Fetched comments count: {}", comments.size());
        comments.forEach(comment -> log.info("  Comment: id={}, memberId={}, content={}", comment.getId(), comment.getMember() != null ? comment.getMember().getUsername() : "N/A", comment.getContent()));

        return comments.stream()
                .map(reviewComment -> {
                    ReviewCommentDTO reviewCommentDTO = modelMapper.map(reviewComment, ReviewCommentDTO.class);
                    // Set nickname for reviewCommentDTO
                    if (reviewComment.getMember() != null) {
                        reviewCommentDTO.setNickname(reviewComment.getMember().getNickname());
                    }
                    return reviewCommentDTO;
                })
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void modify(ReviewCommentDTO reviewCommentDTO, String username) {
        log.info("댓글 수정: {}", reviewCommentDTO);
        ReviewComment reviewComment = reviewCommentRepository.findById(reviewCommentDTO.getId())
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. ID: " + reviewCommentDTO.getId()));

        // 권한 확인
        if (reviewComment.getMember() == null || !reviewComment.getMember().getUsername().equals(username)) {
            throw new IllegalArgumentException("You do not have permission to modify this comment.");
        }

        reviewComment.changeContent(reviewCommentDTO.getContent());
    }

    @Transactional
    @Override
    public void remove(Long commentId, String username) {
        log.info("댓글 삭제: commentId={}", commentId);
        ReviewComment reviewComment = reviewCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다. ID: " + commentId));

        // 권한 확인
        if (reviewComment.getMember() == null || !reviewComment.getMember().getUsername().equals(username)) {
            throw new IllegalArgumentException("You do not have permission to remove this comment.");
        }

        reviewCommentRepository.deleteById(commentId);
    }
}