package webproject_2team.lunch_matching.dto.signup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberUpdateDTO {

//    private Long id; // 수정 대상 회원의 ID

    @NotBlank(message = "닉네임은 필수 입력 값입니다.")
    @Size(min = 2, max = 15, message = "닉네임은 2자 이상 15자 이하로 입력해주세요.")
    private String nickname;

    // 휴대전화번호
    @NotBlank(message = "휴대전화번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$", message = "휴대전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
    @Size(min = 10, max = 13, message = "전화번호는 10자 이상 13자 이하로 입력해주세요. (하이픈 포함)")
    private String phoneNumber;

    // 프로필 이미지 변경을 위한 필드
    private MultipartFile profileImage;
    // 기존 프로필 이미지 삭제 여부
    private boolean deleteProfileImage;

    // 비밀번호 변경은 선택 사항이므로 @NotBlank를 직접 달기보다는 서비스에서 null/empty 체크
    private String currentPassword; // 현재 비밀번호 (선택 사항, 필요시)
    @Size(min = 8, max = 20, message = "새 비밀번호는 8자 이상 20자 이하로 입력해주세요.")
    private String newPassword;     // 새로운 비밀번호
    @Size(min = 8, max = 20, message = "새 비밀번호 확인은 8자 이상 20자 이하로 입력해주세요.")
    private String confirmNewPassword; // 새로운 비밀번호 확인

}
