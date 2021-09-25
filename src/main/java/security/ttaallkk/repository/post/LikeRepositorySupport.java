package security.ttaallkk.repository.post;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.stereotype.Repository;

import security.ttaallkk.domain.post.Like;
import security.ttaallkk.dto.querydsl.LikeCommonDto;

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
     * @return List<Like>
     */
    public List<Like> findLikeOrderByLikeCnt(LocalDateTime from, LocalDateTime to) {
        return jpaQueryFactory.selectFrom(like)
            .innerJoin(like.post, post)
            .fetchJoin()
            .innerJoin(post.writer, member)
            .fetchJoin()
            .orderBy(post.likeCnt.desc())
            .limit(WEEKLY_POST_LIMIT)
            .where(post.createdAt.between(from, to))
            .fetch();
    }   
}
