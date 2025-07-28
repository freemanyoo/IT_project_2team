package webproject_2team.lunch_matching.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import webproject_2team.lunch_matching.domain.CommentEntity;
import webproject_2team.lunch_matching.repository.CommentRepository;

import java.util.List;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentRepository commentRepository;

    // 댓글 등록
    @PostMapping
    public ResponseEntity<CommentEntity> write(@RequestBody CommentEntity comment) {
        CommentEntity saved = commentRepository.save(comment);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    // 댓글 목록 조회 (파티 ID 기준, 오름차순)
    @GetMapping("/party/{partyId}")
    public ResponseEntity<List<CommentEntity>> getComments(@PathVariable Long partyId) {
        List<CommentEntity> list = commentRepository.findByPartyIdOrderByCreatedAtAsc(partyId);
        return ResponseEntity.ok(list);
    }

    // 댓글 수정
    @PutMapping("/{id}")
    public ResponseEntity<CommentEntity> update(@PathVariable Long id, @RequestBody CommentEntity input) {
        return commentRepository.findById(id)
                .map(comment -> {
                    comment.setContent(input.getContent());
                    CommentEntity updated = commentRepository.save(comment);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // 댓글 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (commentRepository.existsById(id)) {
            commentRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    // 특정 파티의 댓글 전체 삭제
    @DeleteMapping("/party/{partyId}")
    public ResponseEntity<Void> deleteByParty(@PathVariable Long partyId) {
        commentRepository.deleteByPartyId(partyId);
        return ResponseEntity.noContent().build();
    }

}
