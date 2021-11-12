package security.ttaallkk.repository.post;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import java.util.List;

import security.ttaallkk.domain.post.Comment;
import security.ttaallkk.dto.querydsl.CommentCommonDto;
import security.ttaallkk.dto.response.CommentPagingResponseDto;
import security.ttaallkk.dto.response.CommentResponseDto;

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
     * 게시글 아이디로 댓글조회
     * @param postId
     * @return List<Comment>
     */
    public List<Comment> findCommentByPostId(Long postId) {
        return jpaQueryFactory
                .selectFrom(comment)
                .leftJoin(comment.parent)
                .fetchJoin()
                .where(comment.post.id.eq(postId))
                .orderBy(
                    comment.parent.id.asc().nullsFirst(),
                    comment.createdAt.asc()
                ).fetch();
    }

    /**
     * 게시글 아이디로 댓글조회 : 최상위 부모 댓글만 페이징 조회
     * @param postId 게시글 아이디
     * @param pageable
     * @return Page<CommentCommonPagingDto>
     */
    public Page<CommentPagingResponseDto> findCommentByPostIdForPaging(Long postId, Pageable pageable) {

        JPAQuery<Comment> query = jpaQueryFactory
            .selectFrom(comment)
            .leftJoin(comment.parent)
            .fetchJoin()
            .innerJoin(comment.writer, member)
            .fetchJoin()
            .where(
                comment.post.id.eq(postId),
                comment.parent.id.isNull())
            .orderBy(
                comment.parent.id.asc(),
                comment.createdAt.asc());

        // 댓글 조회 쿼리 페이징 적용하여 조회
        List<Comment> comments = getQuerydsl().applyPagination(pageable, query).fetch();

        // 조회된 엔티티 페이징 데이터를 계층형 형태의 dto로 변경
        List<CommentPagingResponseDto> convertList = CommentPagingResponseDto.convertCommentPagingResponseDto(comments);

        // Pageable 인터페이스로 변경 후 반환
        return PageableExecutionUtils.getPage(convertList, pageable, query::fetchCount);
    }

    /**
     * 게시글 아이디 + 부모 댓글 번호로 자식댓글 조회
     * @param postId 게시글 아이디
     * @param parentId 부모 댓글 아이디
     * @param pageable
     * @return Page<CommentPagingResponseDto>
     */
    public Page<CommentPagingResponseDto> findCommentChildrenByParentIdForPaging(Long parentId, Long postId, Pageable pageable) {

        JPAQuery<Comment> query = jpaQueryFactory
            .selectFrom(comment)
            .leftJoin(comment.parent)
            .fetchJoin()
            .innerJoin(comment.writer, member)
            .fetchJoin()
            .where(
                comment.post.id.eq(postId),
                comment.parent.id.eq(parentId))
            .orderBy(
                comment.parent.id.asc(),
                comment.createdAt.asc());

        // 댓글 조회 쿼리 페이징 적용하여 조회
        List<Comment> comments = getQuerydsl().applyPagination(pageable, query).fetch();

        // 조회된 엔티티 페이징 데이터를 계층형 형태의 dto로 변경
        List<CommentPagingResponseDto> convertList = CommentPagingResponseDto.convertCommentPagingResponseDto(comments);

        // Pageable 인터페이스로 변경 후 반환
        return PageableExecutionUtils.getPage(convertList, pageable, query::fetchCount);
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
