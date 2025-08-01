package webproject_2team.lunch_matching.dto.signup;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import webproject_2team.lunch_matching.domain.signup.MemberRole;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseDTO { //회원정보 조회용

    private Long id;
    private String username;
    private String name;
    private String gender;
    private LocalDate birthDate;
    private String email;
    private String nickname;
    private String phoneNumber;
    private LocalDateTime regDate;
    private LocalDateTime modDate;
    private Set<MemberRole> roles;

    // 프로필 이미지 URL (클라이언트에서 바로 사용 가능하도록)
    private String profileImageUrl;
    private String profileThumbnailUrl; // 썸네일 경로
}
