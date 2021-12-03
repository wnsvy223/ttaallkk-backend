package security.ttaallkk.repository.common;

import java.util.Iterator;
import java.util.List;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class SortUtils {
    
    /**
     * QueryDsl 조회 결과값 정렬을 위해 Pageable에서 sort 데이터 추출하여 반환
     * @param sort
     * @param pathBuilder
     * @return OrderSpecifier
     */
    @SuppressWarnings({"unchecked", "rawtypes"}) //for OrderSpecifier Gneric
    public static OrderSpecifier<?> getOrderBy(Sort sort, PathBuilder<?> pathBuilder) {
        OrderSpecifier<?> orderBy = null;
        if (!sort.isEmpty()) {
            List<Sort.Order> orders = sort.toList();
            Sort.Order order = orders.get(0);
            Order orderby = order.isAscending() ? Order.ASC : Order.DESC; //오름 or 내림차순 추출
            String property = order.getProperty(); //orderby 타겟 컬럼 추출
            orderBy = new OrderSpecifier(orderby, pathBuilder.get(property)); //추출된 정렬 조건들로 OrderSpecifier 객체 생성
        }
        return orderBy;
    }

    /**
     * Hibernate Search 정렬을 위해 Pageable에서 orderby property 추출하여 반환
     * @param pageable
     * @return String(orderby target property)
     */
    public static String getSearchOrderBy(Pageable pageable) {
        String property = null;
        if (!pageable.getSort().isUnsorted()) {
            Iterator<Sort.Order> orders = pageable.getSort().iterator();
            Sort.Order order = orders.next();
            property = order.getProperty();
        }
        return property;
    }
}
