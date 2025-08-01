package webproject_2team.lunch_matching.security.dto; // 패키지 경로는 실제 위치에 맞게 수정하세요.

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User; // Spring Security의 User 클래스
import java.util.Collection;

@Getter // 추가 정보(nickname, email)를 외부에서 사용할 수 있도록 Getter 추가
public class CustomUserDetails extends User {

    // 추가적으로 저장하고 싶은 사용자 정보
    private final String nickname;
    private final String email;
    private final String gender;

    public CustomUserDetails(
            String username,
            String password,
            Collection<? extends GrantedAuthority> authorities,
            // 추가 정보들을 생성자 파라미터로 받습니다.
            String nickname,
            String email,
            String gender
    ) {
        // 부모 클래스인 User의 생성자를 호출합니다.
        super(username, password, authorities);
        // 추가 정보를 초기화합니다.
        this.nickname = nickname;
        this.email = email;
        this.gender = gender;
    }

    // 관리자 권한을 가지고 있는지 확인하는 헬퍼 메서드 추가
    public boolean isAdmin() {
        // "ROLE_ADMIN" 권한을 가지고 있는지 확인합니다.
        return getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
    }
}
