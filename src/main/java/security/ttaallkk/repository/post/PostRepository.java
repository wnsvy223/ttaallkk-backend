package security.ttaallkk.repository.post;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import security.ttaallkk.domain.post.Post;

public interface PostRepository extends JpaRepository<Post, Long>{
    
    Optional<Post> findPostById(Long id);

    Optional<Post> findPostByWriter(String writer);
}
