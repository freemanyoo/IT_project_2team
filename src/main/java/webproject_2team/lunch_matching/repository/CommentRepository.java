package webproject_2team.lunch_matching.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import webproject_2team.lunch_matching.domain.CommentEntity;

import java.util.List;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    List<CommentEntity> findByPartyIdOrderByCreatedAtAsc(Long partyId);
    List<CommentEntity> findByPartyIdOrderByCreatedAtDesc(Long partyId);

    void deleteByPartyId(Long partyId);
}
