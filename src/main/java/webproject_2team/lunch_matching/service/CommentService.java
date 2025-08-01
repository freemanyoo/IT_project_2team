package webproject_2team.lunch_matching.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import webproject_2team.lunch_matching.domain.Board;
import webproject_2team.lunch_matching.domain.Comment;
import webproject_2team.lunch_matching.repository.BoardRepository;
import webproject_2team.lunch_matching.repository.CommentRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;

    @Transactional(readOnly = true)
    public List<Comment> getCommentsByBoardId(Long boardId) {
        return commentRepository.findByBoardIdOrderByCreatedAtAsc(boardId);
    }

    /**
     * 댓글을 저장합니다. (닉네임, 이메일 저장 기능 추가)
     * @param writerNickname 작성자의 닉네임 (화면 표시용)
     * @param writerEmail 작성자의 이메일 (권한 확인용)
     */
    public Comment saveComment(Long boardId, String content, String writerNickname, String writerEmail) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        if (board.isExpired()) {
            throw new IllegalStateException("마감된 게시글에는 댓글을 작성할 수 없습니다.");
        }
        Comment comment = Comment.builder()
                .board(board)
                .content(content)
                .writer(writerNickname)
                .writerEmail(writerEmail) // 이메일 저장
                .createdAt(LocalDateTime.now())
                .build();
        return commentRepository.save(comment);
    }

    /**
     * 댓글을 삭제합니다. (권한 확인 기능 추가)
     * @param userEmail 현재 로그인한 사용자의 이메일
     * @param isAdmin 현재 로그인한 사용자가 관리자(ROLE_ADMIN)인지 여부
     */
    public void deleteComment(Long id, String userEmail, boolean isAdmin) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 댓글을 찾을 수 없습니다."));
        // ===== 권한 확인 로직 시작 =====
        if (!(isAdmin || (comment.getWriterEmail() != null && comment.getWriterEmail().equals(userEmail)))) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }
        // ===== 권한 확인 로직 끝 =====
        commentRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long getCommentCount(Long boardId) {
        return commentRepository.countByBoardId(boardId);
    }

    /**
     * 댓글을 수정합니다. (권한 확인 기능 추가)
     * @param userEmail 현재 로그인한 사용자의 이메일
     * @param isAdmin 현재 로그인한 사용자가 관리자(ROLE_ADMIN)인지 여부
     */
    public Comment updateComment(Long commentId, String newContent, String userEmail, boolean isAdmin) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("수정할 댓글을 찾을 수 없습니다."));
        // ===== 권한 확인 로직 시작 =====
        if (!(isAdmin || (comment.getWriterEmail() != null && comment.getWriterEmail().equals(userEmail)))) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }
        // ===== 권한 확인 로직 끝 =====
        comment.setContent(newContent);
        return comment;
    }
}