package security.ttaallkk.repository.post;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import security.ttaallkk.domain.member.Member;
import security.ttaallkk.domain.post.Post;
import security.ttaallkk.domain.post.UnLike;

public interface UnLikeRepository extends JpaRepository<UnLike, Long>,  QuerydslPredicateExecutor<UnLike>{
    
    Optional<UnLike> findByPostAndMember(Post post, Member member);
}
