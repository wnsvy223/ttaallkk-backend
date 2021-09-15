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

import java.util.List;

@Repository
public class LikeRepositorySupport extends QuerydslRepositorySupport{
    
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
}
