package webproject_2team.lunch_matching.repository.search;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import webproject_2team.lunch_matching.domain.QReview;
import webproject_2team.lunch_matching.domain.Review;
import webproject_2team.lunch_matching.dto.PageRequestDTO;

import java.util.List;

@Log4j2
public class ReviewSearchImpl extends QuerydslRepositorySupport implements ReviewSearch {

    public ReviewSearchImpl() {
        super(Review.class);
    }

    @Override
    public Page<Review> searchAll(PageRequestDTO pageRequestDTO, Pageable pageable) {
        QReview review = QReview.review;
        JPQLQuery<Review> query = from(review);

        query.leftJoin(review.fileList).fetchJoin(); // fileList를 Fetch Join

        // 검색 조건 추가
        if (pageRequestDTO.getType() != null && !pageRequestDTO.getType().isEmpty() && pageRequestDTO.getKeyword() != null) {
            BooleanBuilder builder = new BooleanBuilder();
            String type = pageRequestDTO.getType();
            switch (type) {
                case "c": // content
                    builder.or(review.content.contains(pageRequestDTO.getKeyword()));
                    break;
                case "m": // menu
                    builder.or(review.menu.contains(pageRequestDTO.getKeyword()));
                    break;
                case "p": // place
                    builder.or(review.place.contains(pageRequestDTO.getKeyword()));
                    break;
                case "w": // member_id (writer)
                    builder.or(review.member_id.contains(pageRequestDTO.getKeyword()));
                    break;
            }
            query.where(builder);
        }

        // review_id > 0 조건 추가 (기본 조건)
        query.where(review.review_id.gt(0L));

        // 전체 개수를 가져오기 위한 쿼리 (페이징 적용 전)
        JPQLQuery<Review> countQuery = query; // 기존 쿼리 복사

        // 정렬 적용
        pageable.getSort().forEach(sort -> {
            com.querydsl.core.types.Order direction = sort.isAscending() ? com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC;
            String prop = sort.getProperty();
            if (prop.equals("review_id")) {
                query.orderBy(new com.querydsl.core.types.OrderSpecifier<>(direction, review.review_id));
            }
            // 다른 정렬 기준이 있다면 여기에 추가
        });

        // Querydsl 페이징 적용
        query.offset(pageable.getOffset());
        query.limit(pageable.getPageSize());

        List<Review> list = query.fetch(); // 페이징된 결과
        long totalCount = countQuery.fetchCount(); // 전체 개수

        return new PageImpl<>(list, pageable, totalCount);
    }
}