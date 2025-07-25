package webproject_2team.lunch_matching.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 신고 정보를 나타내는 엔티티 클래스입니다.
 * 데이터베이스의 `reports` 테이블과 매핑됩니다.
 */
@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    /**
     * 신고 ID (기본 키).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 신고 대상 게시글의 ID.
     */
    @Column(name = "board_id", nullable = false)
    private Long boardId;

    /**
     * 신고를 한 사용자의 이름 또는 식별자.
     */
    @Column(name = "reporter", nullable = false)
    private String reporter;

    /**
     * 신고 사유.
     */
    @Column(name = "reason", nullable = false, columnDefinition = "TEXT")
    private String reason;

    /**
     * 신고가 접수된 날짜 및 시간.
     */
    @Column(name = "report_date", nullable = false)
    private LocalDateTime reportDate;

    /**
     * 신고 처리 상태.
     */
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PENDING;

    /**
     * 엔티티가 영속화되기 전에 신고 날짜를 현재 시간으로 설정합니다.
     */
    @PrePersist
    protected void onCreate() {
        this.reportDate = LocalDateTime.now();
    }

    /**
     * 신고 처리 상태를 나타내는 열거형.
     */
    public enum ReportStatus {
        PENDING,    // 대기중
        REVIEWED,   // 검토완료
        RESOLVED    // 처리완료
    }
}