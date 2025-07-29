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
    private String content;
    private String menu;
    private String place;
    private int rating;
    private String emotion;
    private LocalDateTime regDate;
    private LocalDateTime modDate;
    private List<UploadResultDTO> uploadFileNames; // 파일 업로드 결과 DTO 리스트
}
