package webproject_2team.lunch_matching.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import webproject_2team.lunch_matching.domain.Board;
import webproject_2team.lunch_matching.dto.PageRequestDTO;
import webproject_2team.lunch_matching.dto.PageResponseDTO;
import webproject_2team.lunch_matching.repository.BoardRepository;

import java.time.LocalDateTime;
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

        // 검색 조건이 있는 경우
        if (keyword != null && !keyword.trim().isEmpty() && types != null) {
            boolean searchTitle = false;
            boolean searchContent = false;
            boolean searchWriter = false;

            for (String type : types) {
                switch (type) {
                    case "t": // title
                        searchTitle = true;
                        break;
                    case "c": // content
                        searchContent = true;
                        break;
                    case "w": // writer
                        searchWriter = true;
                        break;
                }
            }

            result = boardRepository.findByKeywordAndTypeAndFilters(
                    keyword, searchTitle, searchContent, searchWriter,
                    genderFilter, foodFilter, pageable);
        }
        // 필터만 있는 경우
        else if ((genderFilter != null && !genderFilter.trim().isEmpty()) ||
                (foodFilter != null && !foodFilter.trim().isEmpty())) {
            result = boardRepository.findByFilters(genderFilter, foodFilter, pageable);
        }
        // 조건이 없는 경우
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

    public Board read(Long id) {
        Optional<Board> result = boardRepository.findById(id);
        return result.orElse(null);
    }

    public void delete(Long id) {
        boardRepository.deleteById(id);
    }

    public void modify(Board board) {
        // 마감시간 재계산
        if (board.getDeadlineHours() != null) {
            board.setDeadlineAt(board.getCreatedAt().plusHours(board.getDeadlineHours()));
        }
        boardRepository.save(board);
    }

    public Board save(Board board) {
        // 마감시간 설정
        if (board.getDeadlineHours() != null) {
            board.setDeadlineAt(board.getCreatedAt().plusHours(board.getDeadlineHours()));
        }
        return boardRepository.save(board);
    }

    // 성별 접근 권한 체크
    public boolean canAccessBoard(Board board, String userGender) {
        if (board.getGenderLimit() == null || board.getGenderLimit().equals("성별상관무")) {
            return true;
        }
        return board.getGenderLimit().equals(userGender);
    }
}