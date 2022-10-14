package security.ttaallkk.service.member;

import java.util.List;
import java.util.stream.Collectors;

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
import security.ttaallkk.domain.member.Friend;
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

    /**
     * Email 또는 DisplyName을 이용해 사용자 검색(Full Text Search)
     * @param keyword 검색키워드
     * @return List<Member>
     */
    @Transactional
    @SuppressWarnings("unchecked")
    public Slice<MemberSearchResponseDto> searchMemberByEmailOrDisplayName(String keyword, Pageable pageable, List<Friend> friends, String uid){
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
                .matching(keyword + "*")
                .createQuery()
            )
            .should(queryBuilder
                .keyword()
                .wildcard()
                .onField("displayName")
                .matching(keyword + "*")
                .createQuery()
            )
            .createQuery();

        FullTextQuery fullTextQuery = fullTextEntityManager.createFullTextQuery(query, Member.class);

        fullTextQuery.setFirstResult((int)pageable.getOffset()); //Offset
        fullTextQuery.setMaxResults(pageable.getPageSize()); //limit
        
        List<Member> members = fullTextQuery.getResultList(); //검색결과
        
        List<Member> filteredFriends = getfilteredMembers(members, friends); //이미 친구인 유저 검색 결과 제외 필터링

        List<Member> filteredMe = getfilteredMe(filteredFriends, uid); //본인 제외 필터링

        boolean hasNext = members.size() >= pageable.getPageSize(); //다음 페이지 존재 유무
        
        List<MemberSearchResponseDto> result = MemberSearchResponseDto.convertMemberSearchResponseDto(filteredMe); //Dto 변환

        return new SliceImpl<>(result, pageable, hasNext);
    }

    /**
     * 컨트롤에서 현재 접속 유저의 친구 목록을 조회한뒤 검색된 유저 목록과 친구목록을 비교하여 현재 친구관계인 유저 필터링 후 반환
     * @param members 검색된 유저 목록
     * @param friends 현재 접속된 유저의 친구 목록
     * @return List<Member> 필터링 된 유저 목록
     */
    private List<Member> getfilteredMembers(List<Member> members, List<Friend> friends) {
        List<Member> filteredMembers = members
            .stream()
            .filter(m -> (
                friends
                    .stream()
                        .filter(f -> f.getFrom().getUid().equals(m.getUid()) || f.getTo().getUid().equals(m.getUid())).count()) < 1)
            .collect(Collectors.toList());
        
        return filteredMembers;
    }

    /**
     * 검색결과에서 본인 제외
     * @param members 검색된 유저 목록
     * @param uid 현재 접속된 유저의 uid
     * @return List<Member> 본인 필터링 된 유저 목록
     */
    private List<Member> getfilteredMe(List<Member> members, String uid) {
        List<Member> filteredMembers = members
            .stream()
            .filter(m -> !m.getUid().equals(uid))
            .collect(Collectors.toList());
        
        return filteredMembers;
    }
}
