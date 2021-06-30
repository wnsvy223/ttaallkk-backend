package security.ttaallkk.repository.post;

import java.util.List;

import com.querydsl.jpa.impl.JPAQueryFactory;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import security.ttaallkk.domain.post.Post;
import static security.ttaallkk.domain.post.QPost.post;
import static security.ttaallkk.domain.member.QMember.member;;

@Repository
public class PostRepositorySupport extends QuerydslRepositorySupport {
    
    private JPAQueryFactory jpaQueryFactory;

    public PostRepositorySupport(JPAQueryFactory jpaQueryFactory) {
        super(Post.class);
        this.jpaQueryFactory = jpaQueryFactory;
    }

    public List<Post> findPostByUid(final String uid) {
        return jpaQueryFactory
            .selectFrom(post)
            .leftJoin(member).on(post.writer.uid.eq(uid))
            .fetch();
    }
    
}
