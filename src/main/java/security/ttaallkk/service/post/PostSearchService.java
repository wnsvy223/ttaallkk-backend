package security.ttaallkk.service.post;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;
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
     * @return
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public Page<Post> searchPostByTitleOrContent(String keyword, Pageable pageable){
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);

        QueryBuilder queryBuilder = fullTextEntityManager
            .getSearchFactory()
            .buildQueryBuilder()
            .forEntity(Post.class)
            .get();     
        
        Query query = queryBuilder.bool()
            .should(queryBuilder
                .keyword()
                .wildcard()
                .onField("title")
                .matching("*" + keyword + "*")
                .createQuery()
            )
            .should(queryBuilder
                .keyword()
                .wildcard()
                .onField("content")
                .matching("*" + keyword + "*")
                .createQuery()
            )
            .createQuery();

        FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery(query, Post.class);

        Sort sort = queryBuilder.sort().byField("id_sort").desc().createSort(); //SortableField로 설정된 엔티티의 id필드를 기준으로 내림차순으로 정렬하도록 설정
        fullTextQuery.setSort(sort);

        int total = fullTextQuery.getResultSize(); //검색결과 게시글 전체 갯수
        int offset = (int)pageable.getOffset(); //시작점
        int pageSize = pageable.getPageSize(); //페이지 사이즈(페이지당 게시글 갯수)
        fullTextQuery.setFirstResult(offset); //검색결과값의 시작점을 pageable의 offset값으로 설정
        if(total - offset >= pageSize){
            fullTextQuery.setMaxResults(pageSize); //검색결과 갯수가 페이지사이즈와 같거나 클경우 설정된 페이지 사이즈만큼 반환
        }else{
            fullTextQuery.setMaxResults(total - offset); //작을경우 해당 남은 갯수만큼 반환
        }
        List<Post> result = fullTextQuery.getResultList(); //검색결과 데이터셋

        return new PageImpl<>(result, pageable, total);
    }
}
