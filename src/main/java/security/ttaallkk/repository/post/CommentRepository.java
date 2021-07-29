package security.ttaallkk.repository.post;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;

import security.ttaallkk.domain.post.Comment;

public interface CommentRepository extends JpaRepository<Comment, Long>, QuerydslPredicateExecutor<Comment>{
    
    @Query("select c from Comment c left join fetch c.parent where c.id = :commentId")
    Optional<Comment> findCommentByIdWithParent(@Param("commentId") Long commentId);
}
