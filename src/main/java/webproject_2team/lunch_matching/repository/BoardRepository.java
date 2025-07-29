package webproject_2team.lunch_matching.repository;

import webproject_2team.lunch_matching.domain.Board;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * `Board` 엔티티에 대한 데이터베이스 접근을 처리하는 리포지토리 인터페이스입니다.
 * Spring Data JPA의 `JpaRepository`를 상속받아 기본적인 CRUD 기능을 제공합니다.
 */
public interface BoardRepository extends JpaRepository<Board, Long> {
    // JpaRepository에 의해 기본적인 CRUD 메서드 (save, findById, findAll, delete 등)가 자동으로 제공됩니다.
    // 추가적인 쿼리 메서드가 필요할 경우 여기에 선언할 수 있습니다.
}
