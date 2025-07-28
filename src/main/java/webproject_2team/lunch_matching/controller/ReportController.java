package webproject_2team.lunch_matching.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;
import webproject_2team.lunch_matching.domain.Board;
import webproject_2team.lunch_matching.domain.Report;
import webproject_2team.lunch_matching.domain.User;
import webproject_2team.lunch_matching.dto.ReportDisplayDTO;
import webproject_2team.lunch_matching.dto.ReportRequestDTO;
import webproject_2team.lunch_matching.repository.BoardRepository;
import webproject_2team.lunch_matching.repository.ReportRepository;
import webproject_2team.lunch_matching.repository.UserRepository;
import webproject_2team.lunch_matching.service.BoardService;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 신고 관련 요청을 처리하는 컨트롤러 클래스입니다.
 * 게시글 신고 및 관리자용 신고 목록 조회, 상태 업데이트 기능을 제공합니다.
 */
@Controller
@RequiredArgsConstructor
public class ReportController {

    private final ReportRepository reportRepository;
    private final BoardService boardService;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    /**
     * 게시글 신고를 접수합니다.
     * 중복 신고를 방지하고, 신고 사유에 비속어 필터링을 적용합니다.
     * 신고 횟수가 3회 이상이면 게시글을 블라인드 처리하고,
     * 신고 횟수가 15회 이상이면 해당 게시글 작성자를 12시간 동안 정지시킵니다.
     *
     * @param reportRequestDTO 신고 요청 데이터 (게시글 ID, 신고 사유)
     * @param principal        현재 로그인한 사용자 정보
     * @param rttr             리다이렉트 시 사용할 속성
     * @return 게시글 목록 페이지로 리다이렉트
     */
    @PostMapping("/report/submit")
    @Transactional
    public String submitReport(@ModelAttribute ReportRequestDTO reportRequestDTO, Principal principal, RedirectAttributes rttr,
                               @RequestParam(value = "simulatedReporter", required = false) String simulatedReporter) {

        String reporter = (simulatedReporter != null && !simulatedReporter.isEmpty()) ? simulatedReporter : ((principal != null) ? principal.getName() : "anonymous");

        // 중복 신고 확인
        boolean exists = reportRepository.findByBoardIdAndReporter(reportRequestDTO.getBoardId(), reporter).isPresent();
        if (exists) {
            rttr.addFlashAttribute("error", "이미 신고한 게시글입니다.");
            return "redirect:/board/list";
        }

        // 신고 사유에 비속어 필터링 적용
        String filteredReason = boardService.filterBadWordsForUser(reportRequestDTO.getReason());

        // 비속어 포함 여부 확인 (관리자 알림용)
        String adminMessage = boardService.checkBadWordsForAdmin(reportRequestDTO.getReason());

        // 신고 정보 저장
        Report report = new Report();
        report.setBoardId(reportRequestDTO.getBoardId());
        report.setReporter(reporter);
        report.setReason(filteredReason); // 필터링된 사유 저장

        // 비속어가 포함된 경우 상태를 별도로 표시할 수 있음
        if (adminMessage != null) {
            // 로그나 별도 처리 로직 추가 가능
            System.out.println("신고 사유에 부적절한 내용 포함: " + adminMessage);
        }

        reportRepository.save(report);

        // ✅ 신고 저장 후 누적 횟수 확인
        Long reportCount = reportRepository.countReportsByBoardId(reportRequestDTO.getBoardId());

        if (reportCount >= 3) {
            boardRepository.findById(reportRequestDTO.getBoardId()).ifPresent(board -> {
                board.blindPost("신고 누적");
                boardRepository.save(board);
            });
            rttr.addFlashAttribute("success", "신고가 접수되었습니다. 신고 누적으로 인해 게시글이 블라인드 처리되었습니다.");
            System.out.println("게시글 ID " + reportRequestDTO.getBoardId() + "이(가) 블라인드 처리되었습니다. (신고 " + reportCount + "회)");
        } else {
            rttr.addFlashAttribute("success", "신고가 접수되었습니다. 검토 후 처리하겠습니다.");
        }

        // 사용자 신고 횟수 처리
        Board reportedBoard = boardRepository.findById(reportRequestDTO.getBoardId()).orElse(null);
        if (reportedBoard != null) {
            User reportedUser = reportedBoard.getUser();
            if (reportedUser != null) {
                // 사용자 ID를 사용하여 User 엔티티를 다시 조회하여 영속성 컨텍스트에 의해 관리되는 엔티티를 사용
                User managedUser = userRepository.findById(reportedUser.getId()).orElse(null);
                if (managedUser != null) {
                    if (managedUser.getSuspendedUntil() != null && managedUser.getSuspendedUntil().isAfter(LocalDateTime.now())) {
                        // 이미 정지된 사용자
                    } else {
                        if (managedUser.getSuspendedUntil() != null) {
                            // 정지 기간이 만료된 경우
                            managedUser.setReportCount(0);
                            managedUser.setSuspendedUntil(null);
                        }

                        System.out.println("Before update: User " + managedUser.getUsername() + ", Report Count: " + managedUser.getReportCount());
                        managedUser.setReportCount(managedUser.getReportCount() + 1);
                        System.out.println("After increment: User " + managedUser.getUsername() + ", Report Count: " + managedUser.getReportCount());
                        if (managedUser.getReportCount() >= 15) {
                            managedUser.setSuspendedUntil(LocalDateTime.now().plusHours(12));
                            managedUser.setReportCount(0); // 정지 후 신고 횟수 초기화
                        }
                        userRepository.save(managedUser);
                        System.out.println("After save: User " + managedUser.getUsername() + ", Report Count: " + managedUser.getReportCount());
                    }
                }
            }
        }

        return "redirect:/board/list";
    }

    /**
     * 관리자용 신고 목록 페이지를 조회합니다.
     * 대기 중인 신고와 모든 신고 목록을 모델에 추가하여 뷰로 전달합니다.
     * @param model 뷰로 데이터를 전달하기 위한 Model 객체
     * @return 관리자 신고 목록 페이지의 뷰 이름
     */
    @GetMapping("/admin/reports")
    public String adminReports(Model model) {
        List<Report> pendingReports = reportRepository.findPendingReports();
        List<Report> allReports = reportRepository.findAllOrderByReportDateDesc();

        // Report를 ReportDisplayDTO로 변환
        List<ReportDisplayDTO> pendingReportsDTO = pendingReports.stream().map(report -> {
            String reportedUsername = "알 수 없음";
            LocalDateTime suspendedUntil = null;
            Board board = boardRepository.findById(report.getBoardId()).orElse(null);
            if (board != null && board.getUser() != null) {
                reportedUsername = board.getUser().getUsername();
                suspendedUntil = board.getUser().getSuspendedUntil();
            }
            return new ReportDisplayDTO(report, reportedUsername, suspendedUntil);
        }).collect(Collectors.toList());

        List<ReportDisplayDTO> allReportsDTO = allReports.stream().map(report -> {
            String reportedUsername = "알 수 없음";
            LocalDateTime suspendedUntil = null;
            Board board = boardRepository.findById(report.getBoardId()).orElse(null);
            if (board != null && board.getUser() != null) {
                reportedUsername = board.getUser().getUsername();
                suspendedUntil = board.getUser().getSuspendedUntil();
            }
            return new ReportDisplayDTO(report, reportedUsername, suspendedUntil);
        }).collect(Collectors.toList());


        model.addAttribute("pendingReports", pendingReportsDTO);
        model.addAttribute("allReports", allReportsDTO);

        return "admin/reports";
    }

    /**
     * 관리자용으로 신고 상태를 업데이트하는 POST 요청을 처리합니다.
     * @param reportId 업데이트할 신고의 ID
     * @param status 변경할 신고 상태 (PENDING, REVIEWED, RESOLVED)
     * @param rttr 리다이렉트 시 사용할 속성
     * @return 관리자 신고 목록 페이지로 리다이렉트
     */
    @PostMapping("/admin/report/update-status")
    public String updateReportStatus(@RequestParam Long reportId,
                                     @RequestParam Report.ReportStatus status,
                                     RedirectAttributes rttr) {

        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("신고를 찾을 수 없습니다."));

        report.setStatus(status);
        reportRepository.save(report);

        rttr.addFlashAttribute("success", "신고 상태가 업데이트되었습니다.");
        return "redirect:/admin/reports";
    }
}