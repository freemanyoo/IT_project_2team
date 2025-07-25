package webproject_2team.lunch_matching.service;

import webproject_2team.lunch_matching.domain.PartyBoardVO;
import webproject_2team.lunch_matching.dto.PageRequestDTO;
import webproject_2team.lunch_matching.dto.PageResponseDTO;

import java.util.List;

public interface PartyBoardService {
    void register(PartyBoardVO vo);
    List<PartyBoardVO> getList();
    PartyBoardVO get(Long id);
    void delete(Long id);
    void update(PartyBoardVO vo);
    PageResponseDTO<PartyBoardVO> getList(PageRequestDTO requestDTO);


}
