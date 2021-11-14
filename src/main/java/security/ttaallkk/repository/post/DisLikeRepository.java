package security.ttaallkk.repository.post;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import security.ttaallkk.domain.member.Member;
import security.ttaallkk.domain.post.Post;
import security.ttaallkk.domain.post.DisLike;

public interface DisLikeRepository extends JpaRepository<DisLike, Long>,  QuerydslPredicateExecutor<DisLike>{
    
    Optional<DisLike> findByPostAndMember(Post post, Member member);
}
