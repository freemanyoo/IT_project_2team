package webproject_2team.lunch_matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewCommentDTO {

    private Long id;
    private Long review_id;
    
    private String nickname; // Add nickname field
    private String content;
    private LocalDateTime regDate;
    private LocalDateTime modDate;

}