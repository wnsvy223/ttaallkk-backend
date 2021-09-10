package security.ttaallkk.repository.post;

import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import security.ttaallkk.domain.post.Post;
import security.ttaallkk.dto.querydsl.PostCommonDto;

import static security.ttaallkk.domain.post.QPost.post;
import static security.ttaallkk.domain.member.QMember.member;

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
     * id 기준으로 최근 게시글 limit 갯수만큼 조회
     * @param num
     * @return List<PostCommonDto>
     */
    public List<PostCommonDto> findPostByRecent(int limit) {
        return jpaQueryFactory
            .select(
                Projections.constructor(
                    PostCommonDto.class, 
                    member.email, 
                    member.displayName,
                    member.profileUrl,
                    post.id,
                    post.title,
                    post.commentCnt,
                    post.likeCnt,
                    post.views,
                    post.postStatus,
                    post.createdAt,
                    post.modifiedAt
                )
            )
            .from(post)
            .innerJoin(post.writer, member)
            .orderBy(post.id.desc())
            .limit(limit)
            .fetch();
    }

    /**
     * 페이징
     * @param pageable
     * @return Page<PostCommonDto>
     */
    public Page<PostCommonDto> paging(Pageable pageable) {
        JPQLQuery<PostCommonDto> query = from(post)
            .innerJoin(post.writer, member)
            .orderBy(post.id.desc())
            .select(
                Projections.constructor(
                    PostCommonDto.class, 
                    member.email, 
                    member.displayName,
                    member.profileUrl,
                    post.id,
                    post.title,
                    post.commentCnt,
                    post.likeCnt,
                    post.views,
                    post.postStatus,
                    post.createdAt,
                    post.modifiedAt
                )
            )
            .fetchAll();            
        List<PostCommonDto> list = getQuerydsl().applyPagination(pageable, query).fetch();
        return PageableExecutionUtils.getPage(list, pageable, query::fetchCount);
    }


    /**
     * Uid사용자의 작성 게시글 검색 by QueryDSL
     * @param uid
     * @return List<PostCommonDto>
     */
    public List<PostCommonDto> findPostByUid(final String uid) {
        return jpaQueryFactory
            .select(
                Projections.constructor(
                    PostCommonDto.class, 
                    member.email, 
                    member.displayName,
                    member.profileUrl,
                    post.id,
                    post.title,
                    post.commentCnt,
                    post.likeCnt,
                    post.views,
                    post.postStatus,
                    post.createdAt,
                    post.modifiedAt
                )
            )
            .from(post)
            .innerJoin(post.writer, member)
            .where(member.uid.eq(uid))
            .fetch();
    }
    
}
