package webproject_2team.lunch_matching.service;

import webproject_2team.lunch_matching.domain.PartyBoardVO;
import webproject_2team.lunch_matching.dto.PageRequestDTO;
import webproject_2team.lunch_matching.dto.PageResponseDTO;
import webproject_2team.lunch_matching.mapper.PartyBoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PartyBoardServiceImpl implements PartyBoardService {

    private final PartyBoardMapper mapper;

    @Override
    public void register(PartyBoardVO vo) {
        mapper.insert(vo);
    }

    @Override
    public List<PartyBoardVO> getList() {
        return mapper.selectAll();
    }

    @Override
    public PartyBoardVO get(Long id) {
        return mapper.selectOne(id);
    }

    @Override
    public void delete(Long id) {
        mapper.delete(id);
    }

    @Override
    public void update(PartyBoardVO vo) {
        mapper.update(vo);
    }

    @Override
    public PageResponseDTO<PartyBoardVO> getList(PageRequestDTO requestDTO) {
        List<PartyBoardVO> list = mapper.selectList(requestDTO);
        int total = mapper.getCountWithFilter(requestDTO);
        int totalPage = (int) Math.ceil((double) total / requestDTO.getSize());

        for (PartyBoardVO vo : list) {
            calculateRemainingTime(vo);

        }
        return PageResponseDTO.<PartyBoardVO>builder()
                .dtoList(list)
                .total(total)
                .page(requestDTO.getPage())
                .size(requestDTO.getSize())
                .totalPage(totalPage)
                .build();

    }

    private void calculateRemainingTime(PartyBoardVO vo) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = vo.getDeadline();

        if (deadline == null) {
            vo.setRemainingTime("마감 시간 없음");
        } else if (deadline.isBefore(now)) {
            vo.setRemainingTime("마감됨");
        } else {
            Duration duration = Duration.between(now, deadline);
            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;

            StringBuilder sb = new StringBuilder();
            if (hours > 0) sb.append(hours).append("시간 ");
            if (minutes > 0) sb.append(minutes).append("분 ");
            sb.append("남음");

            vo.setRemainingTime(sb.toString());
        }
    }









}
