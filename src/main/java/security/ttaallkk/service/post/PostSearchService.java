package security.ttaallkk.service.post;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;


import lombok.extern.log4j.Log4j2;
import security.ttaallkk.domain.post.Post;

@Service
@Log4j2
public class PostSearchService {
    
    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    private final EntityManager entityManager;

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

        fullTextQuery.setFirstResult((int) pageable.getOffset()); //Pageable로부터 넘어온 페이징 목록의 첫 요소값
        fullTextQuery.setMaxResults(pageable.getPageSize()); //Pageable에서 설정된 페이지 사이즈(한 페이지당 요소 갯수)

        List<Post> result = fullTextQuery.getResultList(); //검색결과 데이터셋
        long total = fullTextQuery.getResultSize(); //검색결과 게시글 전체 갯수

        return new PageImpl<>(result, pageable, total);
    }
}
