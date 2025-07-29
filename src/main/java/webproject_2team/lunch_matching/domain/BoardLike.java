package webproject_2team.lunch_matching.domain;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    private String userIdentifier; // 좋아요를 누른 사용자 식별자 (예: 사용자 ID, 세션 ID, IP 주소 등)

    // 복합 유니크 제약 조건 (board_id와 userIdentifier 조합이 유니크해야 함)
    // 한 사용자가 한 게시글에 여러 번 좋아요를 누를 수 없도록 방지
    @TableGenerator(name = "board_like_gen",
            table = "id_gen",
            pkColumnName = "gen_name",
            valueColumnName = "gen_val",
            initialValue = 0,
            allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "board_like_gen")
    @Column(name = "id", nullable = false)
    private Long generatedId;

    @PrePersist
    @PreUpdate
    private void ensureId() {
        if (this.id == null) {
            this.id = generatedId;
        }
    }
}
