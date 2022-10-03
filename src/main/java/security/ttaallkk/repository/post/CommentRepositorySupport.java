package security.ttaallkk.repository.post;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import java.util.List;

import security.ttaallkk.domain.post.Comment;
import security.ttaallkk.domain.post.QComment;
import security.ttaallkk.dto.querydsl.CommentCommonDto;
import security.ttaallkk.dto.querydsl.CommentRootPagingDto;
import security.ttaallkk.dto.response.CommentChildPagingResponseDto;

import static security.ttaallkk.domain.post.QComment.comment;
import static security.ttaallkk.domain.member.QMember.member;
import static security.ttaallkk.domain.post.QPost.post;
import static security.ttaallkk.domain.post.QCategory.category;

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
     * @return Page<CommentRootPagingDto>
     */
    public Page<CommentRootPagingDto> findRootCommentByPostIdForPaging(Long postId, Pageable pageable) {

        // 서브쿼리용 Q엔티티
        QComment subComment = new QComment("sub_comment");

        // 각 루트댓글의 대댓글중에 게시글 작성자의 대댓글 존재여부 확인용 서브 쿼리
        BooleanExpression isContainOwnerComment = JPAExpressions
            .select(subComment.id)
            .from(subComment)
            .innerJoin(subComment.post)
            .where(
                subComment.post.id.eq(postId),
                subComment.parent.id.eq(comment.id),
                subComment.writer.uid.eq(subComment.post.writer.uid)
            )
            .exists();

        // 대댓글 카운트 서브 쿼리
        JPQLQuery<Long> childrenCount = JPAExpressions
            .select(subComment.id.count())
            .from(subComment)
            .where(
                subComment.post.id.eq(postId),
                subComment.parent.id.eq(comment.id)
            );

        JPAQuery<CommentRootPagingDto> query = jpaQueryFactory
            .select(
                Projections.constructor(
                    CommentRootPagingDto.class,
                    comment.id,
                    comment.parent.id,
                    comment.isDeleted,
                    comment.content,
                    comment.writer.uid,
                    comment.writer.email,
                    comment.writer.displayName,
                    comment.writer.profileUrl,
                    comment.createdAt,
                    comment.modifiedAt,
                    childrenCount,
                    isContainOwnerComment,
                    comment.post.writer.uid,
                    comment.post.writer.email,
                    comment.post.writer.displayName,
                    comment.post.writer.profileUrl
                ))
            .from(comment)
            .leftJoin(comment.parent)
            .innerJoin(comment.writer)
            .innerJoin(comment.post.writer)
            .where(
                comment.post.id.eq(postId),
                comment.parent.id.isNull())
            .orderBy(
                comment.parent.id.asc(),
                comment.createdAt.asc());
            
            // 댓글 조회 쿼리 페이징 적용하여 조회
            List<CommentRootPagingDto> comments = getQuerydsl().applyPagination(pageable, query).fetch();

            // 조회된 엔티티 페이징 데이터의 내부값 변경용 함수 호출
            List<CommentRootPagingDto> convertList = CommentRootPagingDto.convertCommentRootPagingDto(comments);
            
            return PageableExecutionUtils.getPage(convertList, pageable, query::fetchCount);
    }

    
    /**
     * 게시글 아이디와 부모 댓글 아이디로 대댓글 조회 : 대댓글 타겟 유저를 표시해주는 방식의 페이징
     * @param postId 게시글 아이디
     * @param parentId 부모 댓글 아이디
     * @param pageable
     * @return Page<CommentChildPagingResponseDto>
     */
    public Page<CommentChildPagingResponseDto> findCommentChildrenByToUserForPaging(Long parentId, Long postId, Pageable pageable) {

        QComment parent = new QComment("parent");
        QComment toComment = new QComment("toComment");

        JPAQuery<Comment> query = jpaQueryFactory
            .selectFrom(comment)
            .leftJoin(comment.parent, parent)
            .fetchJoin()
            .leftJoin(comment.toComment, toComment)
            .fetchJoin()
            .innerJoin(comment.writer, member)
            .fetchJoin()
            .innerJoin(comment.post, post)
            .fetchJoin()
            .innerJoin(post.writer, member)
            .fetchJoin()
            .where(
                comment.post.id.eq(postId),
                comment.parent.id.eq(parentId))
            .orderBy(
                comment.parent.id.asc(),
                comment.createdAt.asc());
        
        // 댓글 조회 쿼리 페이징 적용하여 조회
        List<Comment> comments = getQuerydsl().applyPagination(pageable, query).fetch();

        // 조회된 엔티티 페이징 데이터의 내부값 변경용 함수 호출
        List<CommentChildPagingResponseDto> convertComments = CommentChildPagingResponseDto.convertCommentPagingResponseDto(comments);

        // Pageable 인터페이스로 변경 후 반환
        return PageableExecutionUtils.getPage(convertComments, pageable, query::fetchCount);
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
                        comment.isDeleted,
                        post.id,
                        post.category.ctgTag,
                        post.category.ctgName,
                        post.writer.uid,
                        post.writer.email,
                        post.writer.displayName,
                        post.writer.profileUrl
                    )
                )
            .from(comment)
            .innerJoin(comment.post, post)
            .innerJoin(post.writer, member)
            .innerJoin(post.category, category)
            .orderBy(post.id.desc(), post.createdAt.desc())
            .where(comment.writer.uid.eq(uid))
            .fetch();
    }

}
