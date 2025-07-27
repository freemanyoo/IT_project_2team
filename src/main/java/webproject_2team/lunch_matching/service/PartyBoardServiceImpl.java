package webproject_2team.lunch_matching.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import webproject_2team.lunch_matching.dto.PartyPageRequestDTO;
import webproject_2team.lunch_matching.dto.PartyPageResponseDTO;
import webproject_2team.lunch_matching.domain.PartyBoardEntity;
import webproject_2team.lunch_matching.repository.PartyBoardRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PartyBoardServiceImpl implements PartyBoardService {

    private final PartyBoardRepository repository;

    @Override
    public void register(PartyBoardEntity entity) {
        repository.save(entity);
    }

    @Override
    public List<PartyBoardEntity> getList() {
        List<PartyBoardEntity> list = repository.findAll();
        list.forEach(this::calculateRemainingTime);
        return list;
    }

    @Override
    public PartyBoardEntity get(Long id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }

    @Override
    public void update(PartyBoardEntity entity) {
        repository.save(entity); // save는 insert + update 둘 다 처리
    }

    @Override
    public PartyPageResponseDTO<PartyBoardEntity> getList(PartyPageRequestDTO requestDTO) {

        String keyword = requestDTO.getKeyword() != null ? requestDTO.getKeyword() : "";
        String foodCategory = requestDTO.getFoodCategory() != null ? requestDTO.getFoodCategory() : "";
        String genderLimit = requestDTO.getGenderLimit() != null ? requestDTO.getGenderLimit() : "";

        PageRequest pageable = PageRequest.of(requestDTO.getPage() - 1, requestDTO.getSize());

        Page<PartyBoardEntity> result = repository
                .findByTitleContainingAndFoodCategoryContainingAndGenderLimitContaining(
                        keyword, foodCategory, genderLimit, pageable
                );

        // 남은 시간 계산
        result.getContent().forEach(this::calculateRemainingTime);

        return PartyPageResponseDTO.<PartyBoardEntity>builder()
                .dtoList(result.getContent())
                .total((int) result.getTotalElements())
                .page(requestDTO.getPage())
                .size(requestDTO.getSize())
                .totalPage(result.getTotalPages())
                .build();
    }

    private void calculateRemainingTime(PartyBoardEntity entity) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = entity.getDeadline();

        if (deadline == null) {
            entity.setRemainingTime("마감 시간 없음");
        } else if (deadline.isBefore(now)) {
            entity.setRemainingTime("마감됨");
        } else {
            Duration duration = Duration.between(now, deadline);
            long hours = duration.toHours();
            long minutes = duration.toMinutes() % 60;

            StringBuilder sb = new StringBuilder();
            if (hours > 0) sb.append(hours).append("시간 ");
            if (minutes > 0) sb.append(minutes).append("분 ");
            sb.append("남음");

            entity.setRemainingTime(sb.toString());
        }
    }
}
