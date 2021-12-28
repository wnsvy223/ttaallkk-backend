package security.ttaallkk.repository.member;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import security.ttaallkk.domain.member.Friend;
import security.ttaallkk.domain.member.Member;

public interface FriendRepository extends JpaRepository<Friend, Long>, QuerydslPredicateExecutor<Friend>{
    
    Optional<Friend> findByFromAndTo(Member from, Member to);

    Optional<Friend> findByToAndFrom(Member to, Member from);

    @Query("select f from Friend f "+
    "join fetch f.from "+
    "join fetch f.to "+
    "where f.from.id = :id or f.to.id = :id "+
    "order by f.id asc")
    Slice<Friend> findMyFriendsByUserIdOrderByUid(@Param("id") Long id, Pageable pageable);

    @Query("select f from Friend f "+
    "join fetch f.from "+
    "join fetch f.to "+
    "where f.from.uid = :uid or f.to.uid = :uid "+
    "order by f.id asc")
    List<Friend> findFromOrToByUid(@Param("uid") String uid);
}
