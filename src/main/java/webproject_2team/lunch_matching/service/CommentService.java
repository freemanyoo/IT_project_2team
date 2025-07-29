package webproject_2team.lunch_matching.service;

import webproject_2team.lunch_matching.domain.CommentEntity;

import java.util.List;

public interface CommentService {
    void addComment(CommentEntity comment);
    List<CommentEntity> getCommentsByPartyId(Long partyId);
    void deleteComment(Long id);
    void deleteByPartyId(Long partyId);
    void updateComment(CommentEntity comment);
}

