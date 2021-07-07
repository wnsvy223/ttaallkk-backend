package security.ttaallkk.repository.post;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import security.ttaallkk.domain.post.Post;

public interface PostRepository extends JpaRepository<Post, Long>, QuerydslPredicateExecutor<Post>{
    
    @Query("select p from Post p left join fetch p.writer w where w.uid = :uid")
    List<Post> findPostByWriterUid(@Param("uid") String uid);

    //JPA는 Bulk수행을 제공하지 않기 때문에 jpql로 쿼리로 직접수행 및 @Modifying의 옵션으로 영속성 처리.
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from Post p where p.writer.uid = :uid")
    void deletePostsByUid(@Param("uid") String uid);
}
