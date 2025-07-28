package webproject_2team.lunch_matching.service;

import webproject_2team.lunch_matching.domain.PartyBoardEntity;
import webproject_2team.lunch_matching.dto.PartyPageRequestDTO;
import webproject_2team.lunch_matching.dto.PartyPageResponseDTO;

import java.util.List;

public interface PartyBoardService {
    void register(PartyBoardEntity vo);
    List<PartyBoardEntity> getList();
    PartyBoardEntity get(Long id);
    void delete(Long id);
    void update(PartyBoardEntity vo);
    PartyPageResponseDTO<PartyBoardEntity> getList(PartyPageRequestDTO requestDTO);
}
