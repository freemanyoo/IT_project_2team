package webproject_2team.lunch_matching.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PartyPageResponseDTO<T> {
    private List<T> dtoList;
    private int total;
    private int page;
    private int size;
    private int totalPage;

    public boolean isPrev() {
        return page > 1;
    }

    public boolean isNext() {
        return page < totalPage;
    }

    public int getStart() {
        return Math.max(1, page - 5);
    }

    public int getEnd() {
        return Math.min(totalPage, page + 4);
    }
}
