package security.ttaallkk.service.post;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.Session;
import org.hibernate.search.FullTextSession;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;
import security.ttaallkk.domain.post.Post;
import security.ttaallkk.dto.querydsl.PostCommonDto;
import security.ttaallkk.repository.common.SortUtils;

@Service
@Transactional(readOnly = true)
@Log4j2
public class PostSearchService {
    
    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    public PostSearchService(EntityManager entityManager){
        super();
        this.entityManager = entityManager;
    }

    /**
     * title과 content Full Text Search
     * @param keyword
     * @return Page<PostCommonDto>
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public Page<PostCommonDto> searchPostByCategoryAndTitleOrContent(String keyword,  Long categoryId, Pageable pageable){

        Session session = entityManager.unwrap(Session.class);
        
        FullTextSession fullTextSession = org.hibernate.search.Search.getFullTextSession(session);
        
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

        QueryBuilder queryBuilder = fullTextEntityManager
            .getSearchFactory()
            .buildQueryBuilder()
            .forEntity(Post.class)
            .get();     
        
        //내부 인덱싱된 category_id를 must로 먼저 추출한 뒤 title과 content 검색쿼리를 should로 추출
        //(= 해당 카테고리로 된 게시글 중 title 또는 content에 검색어가 포함될 경우 추출)      
        Query titleQuery = queryBuilder
            .keyword()
            .wildcard()
            .onField("title")
            .matching("*" + keyword + "*")
            .createQuery();
        Query contentQuery = queryBuilder
            .keyword()
            .wildcard()
            .onField("content")
            .matching("*" + keyword + "*")
            .createQuery();
        Query categoryQuery = queryBuilder
            .keyword()
            .onField("category.category_id")
            .matching(categoryId)
            .createQuery();
        
        Query query = queryBuilder.bool()
            .must(categoryQuery)
            .must(queryBuilder.bool()
                .should(titleQuery)
                .should(contentQuery)
                .createQuery())
            .createQuery();

        //Criteria 쿼리로 fetch설정하여 full text query를 생성해야 연관관계 엔티티들을 조인할수있음(5.11.9버전에서 현재사용된 방법은 deprecated됨. 추후 hibernate search 6 으로 migration 필요)
        Criteria criteria = fullTextSession.createCriteria(Post.class)
                .setFetchMode("writer", FetchMode.JOIN)
                .setFetchMode("category", FetchMode.JOIN);
        
        FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery(query, Post.class).setCriteriaQuery(criteria);

        // 정렬
        String orderBy = SortUtils.getSearchOrderBy(pageable); //pageable에서 orderby 타겟 추출
        Sort sort = queryBuilder.sort().byField(orderBy).desc().createSort(); //추출된 orderby propery로 lucene Sort 생성
        fullTextQuery.setSort(sort); //order by절 쿼리에 적용

        // 페이징
        int total = fullTextQuery.getResultSize(); //검색결과 게시글 전체 갯수
        int offset = (int)pageable.getOffset(); //시작점
        int pageSize = pageable.getPageSize(); //페이지 사이즈(페이지당 게시글 갯수)
        fullTextQuery.setFirstResult(offset); //검색결과값의 시작점을 pageable의 offset값으로 설정
        int resultSize = total - offset; //검색 결과 갯수
        if(resultSize > 0){
            if(resultSize >= pageSize){
                fullTextQuery.setMaxResults(pageSize); //검색결과 갯수가 페이지사이즈와 같거나 클경우 설정된 페이지 사이즈만큼 반환
            }else{
                fullTextQuery.setMaxResults(resultSize); //작을경우 해당 남은 갯수만큼 반환
            }
        }
        List<Post> result = fullTextQuery.getResultList(); //검색결과 데이터셋

        List<PostCommonDto> convertResult = PostCommonDto.convertPostCommonDto(result); //댓글 카운트 데이터가 포함된 커스텀 Dto로 변환

        return new PageImpl<>(convertResult, pageable, total);
    }
}
