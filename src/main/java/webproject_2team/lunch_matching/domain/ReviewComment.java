package webproject_2team.lunch_matching.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"review", "member"})
public class ReviewComment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id") // Member 엔티티의 PK인 'id'를 참조합니다.
    private webproject_2team.lunch_matching.domain.signup.Member member;

    @Column(length = 1000, nullable = false)
    private String content; // 댓글 내용

    public void changeContent(String content) {
        this.content = content;
    }

}