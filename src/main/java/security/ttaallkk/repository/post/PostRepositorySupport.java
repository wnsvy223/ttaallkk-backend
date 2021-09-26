package security.ttaallkk.repository.post;

import java.time.LocalDateTime;
import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import security.ttaallkk.common.Constnat;
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
    public List<PostCommonDto> findPostByRecent() {
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
            .limit(Constnat.POST_ROW_LIMIT)
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
    
    /**
     * 주간 좋아요를 받은 숫자가 높은 순으로 좋아요가 속한 게시글 연관데이터 조회
     * @param LocalDateTime from(주간 범위의 시작점 = 월)
     * @param LocalDateTime to(주간 범위의 끝점 = 일)
     * @return List<PostWeeklyLikeDto>
     */
    public List<Post> findPostByWeeklyLike(LocalDateTime from, LocalDateTime to) {
        return jpaQueryFactory
            .select(post)
            .from(post)
            .innerJoin(post.writer, member)
            .fetchJoin()
            .where(post.createdAt.between(from, to), post.likeCnt.gt(0)) //주간 데이터 + 좋아요 수가 0보다 큰경우
            .orderBy(post.likeCnt.desc())
            .limit(Constnat.POST_ROW_LIMIT)
            .fetch();
    }
}
