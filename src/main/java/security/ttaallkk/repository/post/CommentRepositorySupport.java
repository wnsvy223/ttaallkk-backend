package security.ttaallkk.repository.post;

import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import java.util.List;

import security.ttaallkk.domain.post.Comment;

import static security.ttaallkk.domain.post.QComment.comment;


@Repository
public class CommentRepositorySupport extends QuerydslRepositorySupport{
    
    private JPAQueryFactory jpaQueryFactory;

    public CommentRepositorySupport(JPAQueryFactory jpaQueryFactory) {
        super(Comment.class);
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<Comment> findCommentByPostId(Long postId) {
        return jpaQueryFactory.selectFrom(comment)
                .leftJoin(comment.parent)
                .fetchJoin()
                .where(comment.post.id.eq(postId))
                .orderBy(
                    comment.parent.id.asc().nullsFirst(),
                    comment.createdAt.asc()
                ).fetch();
    }
}
