package webproject_2team.lunch_matching.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import webproject_2team.lunch_matching.domain.PartyBoardEntity;

public interface PartyBoardRepository extends JpaRepository<PartyBoardEntity, Long> {

    // 검색 + 필터 조합 페이징
    Page<PartyBoardEntity> findByTitleContainingAndFoodCategoryContainingAndGenderLimitContaining(
            String keyword,
            String foodCategory,
            String genderLimit,
            Pageable pageable
    );
}
