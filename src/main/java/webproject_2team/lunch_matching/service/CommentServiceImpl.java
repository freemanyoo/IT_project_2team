package webproject_2team.lunch_matching.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import webproject_2team.lunch_matching.domain.CommentEntity;
import webproject_2team.lunch_matching.repository.CommentRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Override
    public void addComment(CommentEntity comment) {
        commentRepository.save(comment);
    }

    @Override
    public List<CommentEntity> getCommentsByPartyId(Long partyId) {
        return commentRepository.findByPartyIdOrderByCreatedAtAsc(partyId);
    }


    @Override
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }

    @Override
    public void deleteByPartyId(Long partyId) {
        commentRepository.deleteByPartyId(partyId);
    }

    @Override
    public void updateComment(CommentEntity comment) {
        commentRepository.save(comment); // id 있으면 수정
    }
}

