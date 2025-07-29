package webproject_2team.lunch_matching.repository;

import webproject_2team.lunch_matching.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * User 엔티티에 대한 데이터베이스 접근을 처리하는 리포지토리 인터페이스입니다.
 * Spring Data JPA의 JpaRepository를 상속받아 기본적인 CRUD 기능을 제공합니다.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 사용자 이름(username)으로 User 엔티티를 조회합니다.
     * @param username 조회할 사용자의 이름
     * @return 주어진 사용자 이름에 해당하는 User 엔티티 (존재하지 않을 경우 Optional.empty())
     */
    Optional<User> findByUsername(String username);
}
