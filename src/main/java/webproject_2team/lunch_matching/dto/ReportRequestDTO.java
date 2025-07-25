package webproject_2team.lunch_matching.dto;

import lombok.Data;

/**
 * 게시글 신고 요청을 위한 데이터 전송 객체 (DTO).
 * 클라이언트로부터 게시글 ID와 신고 사유를 받아옵니다.
 */
@Data
public class ReportRequestDTO {
    /**
     * 신고 대상 게시글의 ID.
     */
    private Long boardId;
    /**
     * 신고 사유.
     */
    private String reason;
}

