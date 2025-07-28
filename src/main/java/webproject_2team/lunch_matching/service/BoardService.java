package webproject_2team.lunch_matching.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import webproject_2team.lunch_matching.domain.Board;
import webproject_2team.lunch_matching.dto.PageRequestDTO;
import webproject_2team.lunch_matching.dto.PageResponseDTO;
import webproject_2team.lunch_matching.repository.BoardRepository;

import java.util.List;
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

        // 검색 조건이 없는 경우
        if (keyword == null || keyword.trim().isEmpty() || types == null) {
            result = boardRepository.findAll(pageable);
        } else {
            // 검색 타입에 따른 검색
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

            result = boardRepository.findByKeywordAndType(
                    keyword, searchTitle, searchContent, searchWriter, pageable);
        }

        List<Board> dtoList = result.getContent().stream().collect(Collectors.toList());
        int totalCount = (int)result.getTotalElements();

        return PageResponseDTO.<Board>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .totalCount(totalCount)
                .build();
    }
}