package webproject_2team.lunch_matching.service;


import webproject_2team.lunch_matching.dto.ReviewPageRequestDTO;
import webproject_2team.lunch_matching.dto.ReviewPageResponseDTO;
import webproject_2team.lunch_matching.dto.ReviewDTO;

public interface ReviewService {
    Long register(ReviewDTO reviewDTO, String username);
    ReviewDTO readOne(Long review_id);
    void modify(ReviewDTO reviewDTO, String username);
    void remove(Long review_id, String username);
    ReviewPageResponseDTO<ReviewDTO> getList(ReviewPageRequestDTO reviewPageRequestDTO);
    void updateLikeCount(Long reviewId, int likeCount);
}
