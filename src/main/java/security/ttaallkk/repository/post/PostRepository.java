package security.ttaallkk.repository.post;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import security.ttaallkk.domain.post.Post;

public interface PostRepository extends JpaRepository<Post, Long>{
    
    @Query("select p from Post p left join fetch p.writer w where w.uid = :uid")
    List<Post> findPostByWriterUid(@Param("uid") String uid);
}
