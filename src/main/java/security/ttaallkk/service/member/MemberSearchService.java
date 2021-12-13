package security.ttaallkk.service.member;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.FullTextQuery;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;
import security.ttaallkk.domain.member.Member;
import security.ttaallkk.dto.response.MemberSearchResponseDto;


/**
 * HibernateSearch를 활용한 FullTextSearch 서비스 클래스
 */
@Service
@Transactional(readOnly = true)
@Log4j2
public class MemberSearchService {
    
    @PersistenceContext
    private EntityManager entityManager;

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
    @Transactional
    @SuppressWarnings("unchecked")
    public Slice<MemberSearchResponseDto> searchMemberByEmailOrDisplayName(String keyword, Pageable pageable){
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

        fullTextQuery.setFirstResult((int)pageable.getOffset()); //Offset
        fullTextQuery.setMaxResults(pageable.getPageSize()); //limit
        
        List<Member> members = fullTextQuery.getResultList(); //검색결과
        boolean hasNext = members.size() >= pageable.getPageSize(); //다음 페이지 존재 유무
        
        List<MemberSearchResponseDto> result = MemberSearchResponseDto.convertMemberSearchResponseDto(members);

        return new SliceImpl<>(result, pageable, hasNext);
    }
}
