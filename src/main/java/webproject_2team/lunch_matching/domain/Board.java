package webproject_2team.lunch_matching.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 게시글 정보를 나타내는 엔티티 클래스입니다.
 * 데이터베이스의 'board' 테이블과 매핑됩니다.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Board {

    /**
     * 게시글의 고유 ID (기본 키).
     * 자동 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 게시글의 제목.
     */
    private String title;

    /**
     * 게시글의 내용.
     */
    private String content;

    /**
     * 게시글 작성자의 사용자 이름.
     */
    private String writer;

    /**
     * 게시글 작성자 User 엔티티와의 다대일 관계.
     * 'user_id' 컬럼으로 매핑됩니다.
     */
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    /**
     * 게시글 블라인드 여부.
     * 기본값은 false이며, 데이터베이스에는 'is_blinded' 컬럼으로 저장됩니다.
     */
    @Column(name = "is_blinded", columnDefinition = "boolean default false")
    private boolean isBlinded = false;

    /**
     * 게시글 블라인드 사유.
     * 데이터베이스에는 'blind_reason' 컬럼으로 저장됩니다.
     */
    @Column(name = "blind_reason")
    private String blindReason;

    /**
     * 게시글 블라인드 처리 일시.
     * 데이터베이스에는 'blind_date' 컬럼으로 저장됩니다.
     */
    @Column(name = "blind_date")
    private LocalDateTime blindDate;

    /**
     * 블라인드 처리 전 게시글의 원본 내용.
     * 데이터베이스에는 'original_content' 컬럼으로 저장됩니다.
     */
    @Column(name = "original_content", columnDefinition = "TEXT")
    private String originalContent;

    /**
     * 블라인드 처리 전 게시글의 원본 제목.
     * 데이터베이스에는 'original_title' 컬럼으로 저장됩니다.
     */
    @Column(name = "original_title")
    private String originalTitle;

    /**
     * 게시글을 블라인드 처리합니다.
     * 게시글이 이미 블라인드 상태가 아니라면, 원본 제목과 내용을 저장하고,
     * 제목과 내용을 "경고"로 변경하며, 블라인드 상태를 true로 설정합니다.
     * 블라인드 사유와 일시도 함께 기록합니다.
     * @param reason 블라인드 사유
     */
    public void blindPost(String reason) {
        if (!this.isBlinded) {
            this.originalTitle = this.title;
            this.originalContent = this.content;
            this.title = "경고";
            this.content = "경고";
            this.isBlinded = true;
            this.blindReason = reason;
            this.blindDate = LocalDateTime.now();
        }
    }

    /**
     * 블라인드 처리된 게시글을 해제합니다.
     * 블라인드 처리된 상태이고 원본 제목과 내용이 존재하면,
     * 제목과 내용을 원본으로 되돌리고 블라인드 상태를 false로 설정합니다.
     * 블라인드 사유, 일시, 원본 제목/내용 필드를 초기화합니다.
     */
    public void unblindPost() {
        if (this.isBlinded && this.originalTitle != null && this.originalContent != null) {
            this.title = this.originalTitle;
            this.content = this.originalContent;
            this.isBlinded = false;
            this.blindReason = null;
            this.blindDate = null;
            this.originalTitle = null;
            this.originalContent = null;
        }
    }
}
