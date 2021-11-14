package security.ttaallkk.service.post;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import security.ttaallkk.domain.member.Member;
import security.ttaallkk.domain.post.Like;
import security.ttaallkk.domain.post.Post;
import security.ttaallkk.domain.post.DisLike;
import security.ttaallkk.dto.querydsl.LikeCommonDto;
import security.ttaallkk.dto.request.LikeCreateDto;
import security.ttaallkk.exception.PostNotFoundException;
import security.ttaallkk.exception.UidNotMatchedException;
import security.ttaallkk.repository.member.MemberRepository;
import security.ttaallkk.repository.post.LikeRepository;
import security.ttaallkk.repository.post.LikeRepositorySupport;
import security.ttaallkk.repository.post.PostRepository;
import security.ttaallkk.repository.post.DisLikeRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Log4j2
public class LikeService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final LikeRepository likeRepository;
    private final LikeRepositorySupport likeRepositorySupport;
    private final DisLikeRepository disLikeRepository;
 
     /**
     * 좋아요 등록 : 인증된 사용자가 게시글에 좋아요 등록.
     * case 1: 좋아요 O / 싫어요 X -> 좋아요 해제
     * case 2: 좋아요 X / 싫어요 O -> 좋아요 등록 + 싫어요 해제 
     * case 3: 좋아요 X / 싫어요 X -> 좋아요 등록
     * @param likeCreateDto
     * @return Optional<Like>
     */
    @Transactional
    public Optional<Like> createLike(LikeCreateDto likeCreateDto) {
        Post post = postRepository.findPostByPostId(likeCreateDto.getPostId()).orElseThrow(PostNotFoundException::new);
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findMemberByEmail(email).orElseThrow(() -> new UsernameNotFoundException("이메일이 일치하지 않습니다."));
        if(!isUidAuthenticated(member, likeCreateDto.getUid())) throw new UidNotMatchedException("요청 UID와 인증 UID가 일치하지 않습니다.");
        Optional<Like> like = likeRepository.findByPostAndMember(post, member);
        Optional<DisLike> dislike = disLikeRepository.findByPostAndMember(post, member);
        like.ifPresentOrElse(
            postLike -> { //좋아요가 있을 경우
                likeRepository.delete(postLike); //좋아요 데이터 삭제
                post.decreaseLikeCount(); //좋아요 카운트값 감소
            },
            () -> { //좋아요가 없을 경우
                dislike.ifPresentOrElse(
                    postDisLike -> { //싫어요 있을 경우
                        post.decreaseDisLikeCount(); // 싫어요 카운트값 감소
                        disLikeRepository.delete(postDisLike); //싫어요 데이터 삭제
                        post.increaseLikeCount(); //좋아요 카운트값
                        likeRepository.save(new Like(member, post)); //좋아요 데이터 추가
                    },
                    () -> { //싫어요가 없을 경우
                        post.increaseLikeCount(); //좋아요 카운트값 증가
                        likeRepository.save(new Like(member, post)); //좋아요 데이터 추가
                    } 
                );
            } 
        );

        return like;
    }

    /**
     * 로그인 유저의 좋아요 조회
     * @param uid
     * @return List<LikeResponseDto>
     */
    @Transactional
    public List<LikeCommonDto> getMyLikePost(String uid) {
        List<LikeCommonDto> likes = likeRepositorySupport.findLikeByWriterUid(uid);
        return likes;
    }

    /**
     * RequestDto의 uid와 인증객체의 uid가 동일한지 체크
     * @param member
     * @param uid
     * @return Boolean
     */
    private Boolean isUidAuthenticated(Member member, String uid) {
        return member.getUid().equals(uid) ? true : false;
    }
}
