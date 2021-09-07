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
import security.ttaallkk.dto.querydsl.PostWithMemberDto;

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
     * @return List<PostWithMemberDto>
     */
    public List<PostWithMemberDto> findPostByRecent(int limit) {
        return jpaQueryFactory
            .select(
                Projections.constructor(
                    PostWithMemberDto.class, 
                    member.email, 
                    member.displayName,
                    member.profileUrl,
                    post.id,
                    post.title,
                    post.content,
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
     * @return Page<PostWithMemberDto>
     */
    public Page<PostWithMemberDto> paging(Pageable pageable) {
        JPQLQuery<PostWithMemberDto> query = from(post)
            .innerJoin(post.writer, member)
            .orderBy(post.id.desc())
            .select(
                Projections.constructor(
                    PostWithMemberDto.class, 
                    member.email, 
                    member.displayName,
                    member.profileUrl,
                    post.id,
                    post.title,
                    post.content,
                    post.commentCnt,
                    post.likeCnt,
                    post.views,
                    post.postStatus,
                    post.createdAt,
                    post.modifiedAt
                )
            )
            .fetchAll();            
        List<PostWithMemberDto> list = getQuerydsl().applyPagination(pageable, query).fetch();
        return PageableExecutionUtils.getPage(list, pageable, query::fetchCount);
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
                    post.id,
                    post.title,
                    post.content,
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
