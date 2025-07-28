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

        Page<Board> result = boardRepository.findAll(pageable);

        List<Board> dtoList = result.getContent().stream().collect(Collectors.toList());

        int totalCount = (int)result.getTotalElements();

        return PageResponseDTO.<Board>withAll()
                .pageRequestDTO(pageRequestDTO)
                .dtoList(dtoList)
                .totalCount(totalCount)
                .build();
    }
}
