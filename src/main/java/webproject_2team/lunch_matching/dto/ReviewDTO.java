package webproject_2team.lunch_matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDTO {
    private Long review_id;
    private String member_id;
    private String nickname; // Add nickname field
    private String content;
    private String menu;
    private String place;
    private int rating;
    private String emotion;
    private LocalDateTime regDate;
    private LocalDateTime modDate;
    private List<UploadResultDTO> uploadFileNames; // 파일 업로드 결과 DTO 리스트
    private int likeCount; // 좋아요 수
    private List<ReviewCommentDTO> comments; // 댓글 목록
    private boolean likedByCurrentUser; // 현재 사용자가 좋아요를 눌렀는지 여부

    // 내용이 길 경우 잘라주는 메서드
    public String getTruncatedContent() {
        if (content != null && content.length() > 100) {
            return content.substring(0, 100) + "...";
        }
        return content;
    }
}
