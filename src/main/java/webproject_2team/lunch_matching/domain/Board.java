package webproject_2team.lunch_matching.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String content;

    private String writer;

    private String region;

    private String genderLimit; // 남, 여, 성별상관무

    private String foodCategory; // 한식, 중식 등

    private String imagePath; // 이미지 경로

    private LocalDateTime createdAt;
}
