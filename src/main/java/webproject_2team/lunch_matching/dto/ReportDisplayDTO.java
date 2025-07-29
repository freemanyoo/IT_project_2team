package webproject_2team.lunch_matching.dto;

import webproject_2team.lunch_matching.domain.Report;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReportDisplayDTO {
    private Long id;
    private Long boardId;
    private String reporter;
    private String reason;
    private LocalDateTime reportDate;
    private Report.ReportStatus status;
    private String reportedUsername; // 신고된 게시글 작성자 이름
    private LocalDateTime reportedUserSuspendedUntil; // 신고된 게시글 작성자 정지 만료 시간

    public ReportDisplayDTO(Report report, String reportedUsername, LocalDateTime reportedUserSuspendedUntil) {
        this.id = report.getId();
        this.boardId = report.getBoardId();
        this.reporter = report.getReporter();
        this.reason = report.getReason();
        this.reportDate = report.getReportDate();
        this.status = report.getStatus();
        this.reportedUsername = reportedUsername;
        this.reportedUserSuspendedUntil = reportedUserSuspendedUntil;
    }

    public boolean isReportedUserSuspended() {
        return reportedUserSuspendedUntil != null && reportedUserSuspendedUntil.isAfter(LocalDateTime.now());
    }
}
