package webproject_2team.lunch_matching.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import webproject_2team.lunch_matching.domain.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {
}
