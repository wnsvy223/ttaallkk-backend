package security.ttaallkk.repository.post;

import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import security.ttaallkk.domain.post.Post;
import security.ttaallkk.dto.querydsl.PostWithMemberDto;

import static security.ttaallkk.domain.post.QPost.post;
import static security.ttaallkk.domain.member.QMember.member;;

/**
 * Post Entity QueryDSL Repository
 */
@Repository
public class PostRepositorySupport extends QuerydslRepositorySupport {
    
    private JPAQueryFactory jpaQueryFactory;

    public PostRepositorySupport(JPAQueryFactory jpaQueryFactory) {
        super(Post.class);
        this.jpaQueryFactory = jpaQueryFactory;
    }

    /**
     * Uid사용자의 작성 게시글 검색 by QueryDSL
     * @param uid
     * @return List<PostByMemberDto>
     */
    public List<PostWithMemberDto> findPostByUid(final String uid) {
        return jpaQueryFactory
            .select(
                Projections.constructor(
                    PostWithMemberDto.class, 
                    member.email, 
                    member.displayName,
                    member.profileUrl,
                    post.title,
                    post.content,
                    post.postStatus,
                    post.createdAt,
                    post.modifiedAt)
                )
            .from(post)
            .innerJoin(post.writer, member)
            .where(member.uid.eq(uid))
            .fetch();
    }
    
}
