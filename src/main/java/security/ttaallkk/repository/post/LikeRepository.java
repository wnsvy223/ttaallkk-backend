package security.ttaallkk.repository.post;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import security.ttaallkk.domain.member.Member;
import security.ttaallkk.domain.post.Like;
import security.ttaallkk.domain.post.Post;

public interface LikeRepository extends JpaRepository<Like, Long>,  QuerydslPredicateExecutor<Like>{
    
    Optional<Like> findByPostAndMember(Post post, Member member);

    boolean existsByPostAndMember(Post post, Member member);
}
