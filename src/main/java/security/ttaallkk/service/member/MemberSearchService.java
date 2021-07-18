package security.ttaallkk.service.member;

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
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;
import security.ttaallkk.domain.member.Member;


/**
 * HibernateSearch를 활용한 FullTextSearch 서비스 클래스
 */
@Service
@Log4j2
public class MemberSearchService {
    
    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    private final EntityManager entityManager;

    @Autowired
    public MemberSearchService(EntityManager entityManager){
        super();
        this.entityManager = entityManager;
    }

    public void initializeHibernateSearch(){
        try {
            FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
            fullTextEntityManager.createIndexer().startAndWait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Email 또는 DisplyName을 이용해 사용자 검색(Full Text Search)
     * @param keyword 검색키워드
     * @return List<Member>
     */
    @SuppressWarnings("unchecked")
    public List<Member> searchMemberByEmailOrDisplayName(String keyword){
        FullTextEntityManager fullTextEntityManager = Search.getFullTextEntityManager(entityManager);
        QueryBuilder queryBuilder = fullTextEntityManager
            .getSearchFactory()
            .buildQueryBuilder()
            .forEntity(Member.class)
            .get();     
        
        //email과 displayName 필드를 각각 와일드카드 FullTextSearch후 boolean쿼리로 둘 중 하나 만족 시 조회.
        Query query = queryBuilder.bool()
            .should(queryBuilder
                .keyword()
                .wildcard()
                .onField("email")
                .matching("*" + keyword + "*")
                .createQuery()
            )
            .should(queryBuilder
                .keyword()
                .wildcard()
                .onField("displayName")
                .matching("*" + keyword + "*")
                .createQuery()
            )
            .createQuery();

        FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery(query, Member.class);

        List<Member> result = null;
        try{
            result = (List<Member>)fullTextQuery.getResultList();
            log.info("검색어 : " + keyword + "\n" +"검색결과 : " + result);
        }catch(NoResultException noResultException){
            log.error("검색결과 오류 : " + noResultException.getMessage());
        }
        return result;
    }
}
