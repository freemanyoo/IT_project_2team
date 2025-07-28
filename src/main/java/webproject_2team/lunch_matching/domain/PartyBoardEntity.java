package webproject_2team.lunch_matching.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "party_board")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartyBoardEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "writer_id", nullable = false)
    private Long writerId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "location_name")
    private String locationName;

    private Double latitude;
    private Double longitude;

    @Column(name = "food_category")
    private String foodCategory;

    @Column(name = "gender_limit")
    private String genderLimit;

    @Column(nullable = false)
    private String status; // 예: "OPEN", "CLOSED"

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "party_time")
    private LocalDateTime partyTime;

    private LocalDateTime deadline;

    // DB에는 없는 계산용 필드는 @Transient로 선언
    @Transient
    private String remainingTime;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
