package security.ttaallkk.repository.post;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;
import java.util.List;

import security.ttaallkk.domain.post.Comment;
import security.ttaallkk.dto.querydsl.CommentCommonDto;

import static security.ttaallkk.domain.post.QComment.comment;
import static security.ttaallkk.domain.member.QMember.member;
import static security.ttaallkk.domain.post.QPost.post;

@Repository
public class CommentRepositorySupport extends QuerydslRepositorySupport{
    
    private JPAQueryFactory jpaQueryFactory;

    public CommentRepositorySupport(JPAQueryFactory jpaQueryFactory) {
        super(Comment.class);
        this.jpaQueryFactory = jpaQueryFactory;
    }

    /**
     * 게시글 번호로 댓글조회
     * @param postId
     * @return List<Comment>
     */
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

    /**
     * 댓글 작성자 uid로 댓글 조회(댓글을 단 게시물의 주인 유저 정보와 게시물 아이디값 조인)
     * @param uid
     * @return List<CommentCommonDto>
     */
    public List<CommentCommonDto> findCommentByWriterUid(String uid) {
        return jpaQueryFactory
            .select(
                Projections.constructor(
                    CommentCommonDto.class, 
                        comment.id,
                        comment.content,
                        comment.createdAt,
                        post.id,
                        post.writer.uid,
                        post.writer.email,
                        post.writer.displayName,
                        post.writer.profileUrl
                    )
                )
            .from(comment)
            .innerJoin(comment.post, post)
            .innerJoin(post.writer, member)
            .orderBy(post.id.desc(), post.createdAt.desc())
            .where(comment.writer.uid.eq(uid))
            .fetch();
    }

}
