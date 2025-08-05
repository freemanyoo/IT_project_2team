package webproject_2team.lunch_matching.domain;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter // 추가된 Setter 유지
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(exclude = {"member", "likes", "comments"})
public class Review extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long review_id;// pk 리뷰id

    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "bno")
    // private Board board; // 이 부분 제거

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id") // Member 엔티티의 PK인 'id'를 참조합니다.
    private webproject_2team.lunch_matching.domain.signup.Member member;

    @Column(length = 2000, nullable = false)
    private String content;

    @Column(length = 50, nullable = false)
    private String menu; //메뉴

    @Column(length = 100, nullable = false)
    private String place; //장소

    private int rating; // 평점

    @Column(length = 50)
    private String emotion; // 감정이모션

    @ElementCollection
    @Builder.Default
    private List<UploadResult> fileList = new ArrayList<>();

    @Builder.Default
    private int likeCount = 0; // 좋아요 수

    @OneToMany(mappedBy = "review", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewLike> likes = new ArrayList<>();

    @OneToMany(mappedBy = "review", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewComment> comments = new ArrayList<>();

    public void change(String content, String menu, String place, int rating, String emotion) {
        this.content = content;
        this.menu = menu;
        this.place = place;
        this.rating = rating;
        this.emotion = emotion;
    }

}
