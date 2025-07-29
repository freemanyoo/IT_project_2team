package webproject_2team.lunch_matching.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;    // ①
import org.springframework.stereotype.Service;
import webproject_2team.lunch_matching.domain.CommentEntity;
import webproject_2team.lunch_matching.repository.CommentRepository;

import java.util.List;

@Service
@Slf4j                            // ②
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Override
    public void addComment(CommentEntity comment) {
        // 서비스 진입 로그
        log.info("[Service] addComment() 호출, 파라미터 comment={}", comment);
        CommentEntity saved = commentRepository.save(comment);
        log.info("[Service] commentRepository.save 완료, 저장된 엔티티 id={}", saved.getId());
    }

    @Override
    public List<CommentEntity> getCommentsByPartyId(Long partyId) {
        log.info("[Service] getCommentsByPartyId() 호출 partyId={}", partyId);
        return commentRepository.findByPartyIdOrderByCreatedAtAsc(partyId);
    }

    @Override
    public void updateComment(CommentEntity comment) {
        log.info("[Service] updateComment() 호출 id={}, content={}", comment.getId(), comment.getContent());
        commentRepository.findById(comment.getId()).ifPresent(c -> {
            c.setContent(comment.getContent());
            commentRepository.save(c);
            log.info("[Service] 댓글 수정 완료 id={}", c.getId());
        });
    }

    @Override
    public void deleteComment(Long id) {
        log.info("[Service] deleteComment() 호출 id={}", id);
        commentRepository.deleteById(id);
        log.info("[Service] 댓글 삭제 완료 id={}", id);
    }
}
