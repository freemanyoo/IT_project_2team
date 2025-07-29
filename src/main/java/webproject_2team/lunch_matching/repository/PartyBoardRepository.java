package webproject_2team.lunch_matching.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import webproject_2team.lunch_matching.domain.PartyBoardEntity;

public interface PartyBoardRepository extends JpaRepository<PartyBoardEntity, Long> {

    /**
     * 동적 쿼리를 사용하여 검색어, 음식 카테고리, 성별 제한에 따라 게시글을 조회합니다.
     * 각 파라미터가 null이거나 비어있으면 해당 조건은 무시됩니다.
     */
    @Query("SELECT p FROM PartyBoardEntity p WHERE " +
            "(:keyword IS NULL OR :keyword = '' OR p.title LIKE %:keyword%) AND " +
            "(:foodCategory IS NULL OR :foodCategory = '' OR p.foodCategory = :foodCategory) AND " +
            "(:genderLimit IS NULL OR :genderLimit = '' OR p.genderLimit = :genderLimit)")
    Page<PartyBoardEntity> findPartyBoards(
            @Param("keyword") String keyword,
            @Param("foodCategory") String foodCategory,
            @Param("genderLimit") String genderLimit,
            Pageable pageable
    );
}