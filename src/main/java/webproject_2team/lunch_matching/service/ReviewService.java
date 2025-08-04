package webproject_2team.lunch_matching.service;


import webproject_2team.lunch_matching.dto.ReviewPageRequestDTO;
import webproject_2team.lunch_matching.dto.ReviewPageResponseDTO;
import webproject_2team.lunch_matching.dto.ReviewDTO;

public interface ReviewService {
    Long register(ReviewDTO reviewDTO);
    ReviewDTO readOne(Long review_id);
    void modify(ReviewDTO reviewDTO, String memberId);
    void remove(Long review_id, String memberId);
    ReviewPageResponseDTO<ReviewDTO> getList(ReviewPageRequestDTO reviewPageRequestDTO);
    void updateLikeCount(Long reviewId, int likeCount);
}
