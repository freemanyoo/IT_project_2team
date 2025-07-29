package webproject_2team.lunch_matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyPageRequestDTO {
    // 1. 타입을 int에서 Integer로 변경하여 null 값을 받을 수 있도록 합니다.
    private Integer page;
    private Integer size;

    private String keyword;
    private String foodCategory;
    private String genderLimit;

    // 2. getter에서 null 또는 0 이하의 값이 들어올 경우 기본값을 반환하도록 수정합니다.
    public int getPage() {
        if (this.page == null || this.page <= 0) {
            return 1;
        }
        return this.page;
    }

    public int getSize() {
        if (this.size == null || this.size <= 0) {
            return 10;
        }
        return this.size;
    }

    public int getSkip() {
        // 3. 수정한 getter를 호출하도록 변경합니다.
        return (getPage() - 1) * getSize();
    }
}