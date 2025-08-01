package webproject_2team.lunch_matching.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import webproject_2team.lunch_matching.domain.Board;
import webproject_2team.lunch_matching.dto.PageRequestDTO;
import webproject_2team.lunch_matching.dto.PageResponseDTO;
import webproject_2team.lunch_matching.repository.BoardRepository;

import java.time.LocalDateTime; // LocalDateTime 임포트 추가
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;

    public PageResponseDTO<Board> getBoardList(PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("id");

        Page<Board> result;
        String keyword = pageRequestDTO.getKeyword();
        String[] types = pageRequestDTO.getTypes();
        String genderFilter = pageRequestDTO.getGenderFilter();
        String foodFilter = pageRequestDTO.getFoodFilter();

        if (keyword != null && !keyword.trim().isEmpty()) {
            boolean searchTitle = false;
            boolean searchContent = false;
            boolean searchWriter = false;
            boolean searchRegion = false; // '위치' 검색을 위한 플래그 추가

            if (types == null || types.length == 0) {
                searchTitle = true;
                searchContent = true;
                searchWriter = true;
                searchRegion = true; // '전체' 검색 시 위치도 포함
            } else {
                for (String type : types) {
                    switch (type) {
                        case "t":
                            searchTitle = true;
                            break;
                        case "c":
                            searchContent = true;
                            break;
                        case "w":
                            searchWriter = true;
                            break;
                        case "r": // '위치' 타입(r) 처리
                            searchRegion = true;
                            break;
                    }
                }
            }

            // Repository 호출 시 searchRegion 플래그 전달
            result = boardRepository.findByKeywordAndTypeAndFilters(
                    keyword, searchTitle, searchContent, searchWriter, searchRegion,
                    genderFilter, foodFilter, pageable);
        }
        else if ((genderFilter != null && !genderFilter.trim().isEmpty()) ||
                (foodFilter != null && !foodFilter.trim().isEmpty())) {
            result = boardRepository.findByFilters(genderFilter, foodFilter, pageable);
        }
        else {
            result = boardRepository.findAll(pageable);
        }

        List<Board> dtoList = result.getContent().stream().collect(Collectors.toList());
        int totalCount = (int)result.getTotalElements();

        return PageResponseDTO.<Board>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .totalCount(totalCount)
                .build();
    }

    /**
     * 게시글 ID로 하나의 게시글을 조회합니다.
     * @param id 조회할 게시글의 ID
     * @return 조회된 Board 객체, 없으면 null
     */
    public Board read(Long id) {
        Optional<Board> result = boardRepository.findById(id);
        return result.orElse(null);
    }

    /**
     * 게시글을 삭제합니다. (권한 확인 기능 추가)
     * @param id 삭제할 게시글의 ID
     * @param userEmail 현재 로그인한 사용자의 이메일
     * @param isAdmin 현재 로그인한 사용자가 관리자(ROLE_ADMIN)인지 여부
     */
    @Transactional
    public void delete(Long id, String userEmail, boolean isAdmin) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 게시글을 찾을 수 없습니다."));

        // ===== 권한 확인 로직 시작 =====
        // 관리자(isAdmin)이거나, 게시글 작성자 이메일과 현재 로그인한 사용자 이메일이 일치하는 경우
        // Board 엔티티에 getWriterEmail() 메소드가 있어야 합니다.
        if (!(isAdmin || (board.getWriterEmail() != null && board.getWriterEmail().equals(userEmail)))) {
            throw new AccessDeniedException("삭제 권한이 없습니다.");
        }
        // ===== 권한 확인 로직 끝 =====
        boardRepository.deleteById(id);
    }

    /**
     * 게시글 수정 메서드
     * @param board 수정할 Board 엔티티 (ID, 수정된 내용 포함)
     * @param userEmail 현재 로그인한 사용자의 이메일
     * @param isAdmin 현재 로그인한 사용자가 관리자(ROLE_ADMIN)인지 여부
     */
    @Transactional
    public void modify(Board board, String userEmail, boolean isAdmin) {
        Board existingBoard = boardRepository.findById(board.getId())
                .orElseThrow(() -> new IllegalArgumentException("수정할 게시글을 찾을 수 없습니다."));

        // ===== 권한 확인 로직 시작 =====
        // 관리자(isAdmin)이거나, 게시글 작성자 이메일과 현재 로그인한 사용자 이메일이 일치하는 경우
        if (!(isAdmin || (existingBoard.getWriterEmail() != null && existingBoard.getWriterEmail().equals(userEmail)))) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }
        // ===== 권한 확인 로직 끝 =====

        // 기존 게시글의 생성 시간 유지 (수정 시에는 변경되지 않음)
        LocalDateTime originalCreatedAt = existingBoard.getCreatedAt();

        // Board 엔티티의 change 메서드에 모든 필드를 전달
        existingBoard.change(
                board.getTitle(),
                board.getContent(),
                board.getRegion(),
                board.getGenderLimit(),
                board.getFoodCategory(),
                board.getDeadlineHours(),
                board.getImagePath(), // 기존 imagePath 유지 또는 새 이미지 경로로 업데이트
                board.getLatitude(),   // 지도 관련 필드 추가
                board.getLongitude(),  // 지도 관련 필드 추가
                board.getLocationName()// 지도 관련 필드 추가
        );

        // 마감시간 재계산 (생성 시간 기준)
        if (board.getDeadlineHours() != null && originalCreatedAt != null) {
            existingBoard.setDeadlineAt(originalCreatedAt.plusHours(board.getDeadlineHours()));
        } else if (board.getDeadlineHours() != null) { // 예외 처리: originalCreatedAt이 없는 경우 (거의 없겠지만 방어적 코드)
            existingBoard.setDeadlineAt(LocalDateTime.now().plusHours(board.getDeadlineHours()));
        }

        boardRepository.save(existingBoard);
    }

    // 게시글 저장 메서드 (중복된 save 메서드 제거 후 통합)
    public Board save(Board board) {
        // createdAt이 컨트롤러에서 설정되지 않았다면 여기서 현재 시간으로 설정
        if (board.getCreatedAt() == null) {
            board.setCreatedAt(LocalDateTime.now());
        }

        // 마감시간 설정
        if (board.getDeadlineHours() != null && board.getCreatedAt() != null) {
            board.setDeadlineAt(board.getCreatedAt().plusHours(board.getDeadlineHours()));
        }

        // board 엔티티에 latitude, longitude, locationName 필드가 폼에서 자동으로 매핑되어 들어올 것입니다.
        return boardRepository.save(board);
    }

    // 성별 접근 권한 체크
    public boolean canAccessBoard(Board board, String gender) {
        if (board.getGenderLimit() == null || board.getGenderLimit().equals("성별상관무")) {
            return true;
        }
        return board.getGenderLimit().equals(gender);
    }
}