package security.ttaallkk.repository.post;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import security.ttaallkk.domain.post.Post;

public interface PostRepository extends JpaRepository<Post, Long>, QuerydslPredicateExecutor<Post>{
    
    //게시글 아이디로 게시글 데이터 조회(작성자와 카테고리는 inner fetch join)
    @Query("select p from Post p join fetch p.writer join fetch p.category where p.id = :postId")
    Optional<Post> findPostByPostId(@Param("postId") Long postId);

    //JPA는 Bulk수행을 제공하지 않기 때문에 jpql로 쿼리로 직접수행 및 @Modifying의 옵션으로 영속성 처리.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Post p where p.writer.uid = :uid")
    void deleteAllPostByUid(@Param("uid") String uid);
}
