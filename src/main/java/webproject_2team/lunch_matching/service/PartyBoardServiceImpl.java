package webproject_2team.lunch_matching.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import webproject_2team.lunch_matching.domain.PartyBoardEntity;
import webproject_2team.lunch_matching.dto.PartyPageRequestDTO;
import webproject_2team.lunch_matching.dto.PartyPageResponseDTO;
import webproject_2team.lunch_matching.repository.PartyBoardRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PartyBoardServiceImpl implements PartyBoardService {

    private final PartyBoardRepository partyBoardRepository;

    @Override
    public void register(PartyBoardEntity vo) {
        vo.setCreatedAt(LocalDateTime.now()); // 작성 시 createdAt 자동 설정
        partyBoardRepository.save(vo);
    }

    @Override
    public List<PartyBoardEntity> getList() {
        List<PartyBoardEntity> list = partyBoardRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        list.forEach(this::calculateRemainingTime);
        return list;
    }

    @Override
    public PartyBoardEntity get(Long id) {
        PartyBoardEntity entity = partyBoardRepository.findById(id).orElse(null);
        if (entity != null) {
            calculateRemainingTime(entity);
        }
        return entity;
    }

    @Override
    public void delete(Long id) {
        partyBoardRepository.deleteById(id);
    }

    @Override
    public void update(PartyBoardEntity vo) {
        partyBoardRepository.save(vo); // id가 있으면 수정됨
    }

    @Override
    public PartyPageResponseDTO<PartyBoardEntity> getList(PartyPageRequestDTO requestDTO) {
        Pageable pageable = PageRequest.of(
                requestDTO.getPage() - 1,
                requestDTO.getSize(),
                Sort.by(Sort.Direction.DESC, "id") // 최신순
        );

        Page<PartyBoardEntity> result = partyBoardRepository
                .findByTitleContainingAndFoodCategoryContainingAndGenderLimitContaining(
                        requestDTO.getKeyword(),
                        requestDTO.getFoodCategory(),
                        requestDTO.getGenderLimit(),
                        pageable
                );

        result.getContent().forEach(this::calculateRemainingTime);

        return PartyPageResponseDTO.<PartyBoardEntity>builder()
                .dtoList(result.getContent())
                .total((int) result.getTotalElements())
                .page(requestDTO.getPage())
                .size(requestDTO.getSize())
                .totalPage(result.getTotalPages())
                .build();
    }

    private void calculateRemainingTime(PartyBoardEntity vo) {
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
