package security.ttaallkk.repository.post;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import security.ttaallkk.domain.post.Like;
import security.ttaallkk.dto.querydsl.LikeCommonDto;
import security.ttaallkk.dto.response.LikeWeeklyDto;

import static security.ttaallkk.domain.member.QMember.member;
import static security.ttaallkk.domain.post.QPost.post;
import static security.ttaallkk.domain.post.QLike.like;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public class LikeRepositorySupport extends QuerydslRepositorySupport{
    
    private final long WEEKLY_POST_LIMIT = 7;
    private JPAQueryFactory jpaQueryFactory;

    public LikeRepositorySupport(JPAQueryFactory jpaQueryFactory) {
        super(Like.class);
        this.jpaQueryFactory = jpaQueryFactory;
    }


    /**
     * 로그인 유저가 좋아요 누른 게시글의 아이디값 + 게시글작성자 데이터 조회
     * @param uid
     * @return List<LikeResponseDto>
     */
    public List<LikeCommonDto> findLikeByWriterUid(String uid) {
        return jpaQueryFactory
            .select(
                Projections.constructor(
                    LikeCommonDto.class, 
                      like.id,
                      post.id,
                      post.createdAt,
                      post.likeCnt,
                      post.title,
                      post.writer.uid,
                      post.writer.email,
                      post.writer.displayName,
                      post.writer.profileUrl
                    )
                )
            .from(like)
            .innerJoin(like.post, post)
            .innerJoin(post.writer, member)
            .where(like.member.uid.eq(uid))
            .fetch();
    }

    /**
     * 주간 좋아요를 받은 숫자가 높은 순으로 좋아요가 속한 게시글 연관데이터 조회
     * @param LocalDateTime from(주간 범위의 시작점 = 월)
     * @param LocalDateTime to(주간 범위의 끝점 = 일)
     * @return List<Like>
     */
    public List<LikeWeeklyDto> findLikeOrderByLikeCnt(LocalDateTime from, LocalDateTime to) {
        return jpaQueryFactory
            .select(
                Projections.constructor(
                    LikeWeeklyDto.class, 
                    like.id,
                    post.id,
                    post.title,
                    post.likeCnt,
                    post.createdAt,
                    post.writer.email,
                    post.writer.uid,
                    post.writer.displayName,
                    post.writer.profileUrl
                    )
                )
            .from(like)
            .innerJoin(like.post, post)
            .innerJoin(post.writer, member)
            .orderBy(post.likeCnt.desc())
            .limit(WEEKLY_POST_LIMIT)
            .where(post.createdAt.between(from, to))
            .groupBy(post.id)
            .fetch();
    }   
}
