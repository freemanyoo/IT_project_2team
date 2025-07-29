package webproject_2team.lunch_matching.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 사용자 정보를 나타내는 엔티티 클래스입니다.
 * 데이터베이스의 'user' 테이블과 매핑됩니다.
 */
@Entity
@Getter
@Setter
public class User {
    /**
     * 사용자의 고유 ID (기본 키).
     * 자동 생성됩니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 사용자 이름 (로그인 ID).
     */
    private String username;

    /**
     * 사용자 비밀번호.
     */
    private String password;

    /**
     * 사용자 신고 누적 횟수.
     * 기본값은 0입니다.
     */
    private int reportCount = 0;

    /**
     * 사용자 정지 만료 시간.
     * 이 시간이 현재 시간보다 미래이면 사용자는 정지 상태입니다.
     */
    private LocalDateTime suspendedUntil;
}
