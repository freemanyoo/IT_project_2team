package webproject_2team.lunch_matching.repository;

import com.example.kmj.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Report 엔티티에 대한 데이터베이스 접근을 처리하는 리포지토리 인터페이스입니다.
 * Spring Data JPA의 JpaRepository를 상속받아 기본적인 CRUD 기능을 제공하며,
 * 추가적인 쿼리 메서드를 정의합니다.
 */
@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    /**
     * 특정 게시글에 대한 특정 사용자의 신고 기록을 찾습니다.
     * 중복 신고를 방지하는 데 사용됩니다.
     * @param boardId 게시글의 ID
     * @param reporter 신고자의 사용자 이름
     * @return 해당 신고 기록 (존재하지 않을 경우 Optional.empty())
     */
    Optional<Report> findByBoardIdAndReporter(Long boardId, String reporter);

    /**
     * 특정 게시글에 대한 모든 신고 목록을 조회합니다.
     * @param boardId 게시글의 ID
     * @return 해당 게시글에 대한 신고 목록
     */
    List<Report> findByBoardId(Long boardId);

    /**
     * 특정 사용자가 신고한 모든 목록을 조회합니다.
     * @param reporter 신고자의 사용자 이름
     * @return 해당 사용자가 신고한 목록
     */
    List<Report> findByReporter(String reporter);

    /**
     * 특정 상태(status)를 가진 신고 목록을 조회합니다.
     * @param status 조회할 신고 상태
     * @return 해당 상태의 신고 목록
     */
    List<Report> findByStatus(Report.ReportStatus status);

    /**
     * 특정 게시글의 총 신고 횟수를 조회합니다.
     * @param boardId 게시글의 ID
     * @return 해당 게시글의 신고 횟수
     */
    @Query("SELECT COUNT(r) FROM Report r WHERE r.boardId = :boardId")
    Long countReportsByBoardId(@Param("boardId") Long boardId);

    /**
     * 모든 신고 목록을 신고 날짜(reportDate)를 기준으로 최신순으로 정렬하여 조회합니다.
     * 관리자 페이지에서 전체 신고 내역을 확인할 때 사용됩니다.
     * @return 최신순으로 정렬된 모든 신고 목록
     */
    @Query("SELECT r FROM Report r ORDER BY r.reportDate DESC")
    List<Report> findAllOrderByReportDateDesc();

    /**
     * 처리되지 않은 (PENDING) 상태의 신고 목록을 오래된 신고 날짜 순으로 조회합니다.
     * @return 처리 대기 중인 신고 목록
     */
    @Query("SELECT r FROM Report r WHERE r.status = 'PENDING' ORDER BY r.reportDate ASC")
    List<Report> findPendingReports();
}