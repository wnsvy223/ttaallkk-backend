package security.ttaallkk.service.post;


import java.util.Optional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import security.ttaallkk.domain.member.Member;
import security.ttaallkk.domain.post.Like;
import security.ttaallkk.domain.post.Post;
import security.ttaallkk.domain.post.UnLike;
import security.ttaallkk.dto.request.UnLikeCreateDto;
import security.ttaallkk.exception.PostNotFoundException;
import security.ttaallkk.exception.UidNotMatchedException;
import security.ttaallkk.repository.member.MemberRepository;
import security.ttaallkk.repository.post.LikeRepository;
import security.ttaallkk.repository.post.PostRepository;
import security.ttaallkk.repository.post.UnLikeRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Log4j2
public class UnLikeService {
    
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final UnLikeRepository unLikeRepository;
    private final LikeRepository likeRepository;
 
    /**
     * 싫어요 등록 : 인증된 사용자가 게시글에 싫어요 등록.
     * case 1: 싫어요 O / 좋아요 X -> 싫어요 해제
     * case 2: 싫어요 X / 좋아요 O -> 싫어요 등록 + 좋아요 해제 
     * case 3: 싫어요 X / 좋아요 X -> 싫어요 등록
     * @param likeCreateDto
     * @return Optional<Like>
     */
    @Transactional
    public Optional<UnLike> createUnLike(UnLikeCreateDto unlikeCreateDto) {
        Post post = postRepository.findPostByPostId(unlikeCreateDto.getPostId()).orElseThrow(PostNotFoundException::new);
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findMemberByEmail(email).orElseThrow(() -> new UsernameNotFoundException("이메일이 일치하지 않습니다."));
        if(!isUidAuthenticated(member, unlikeCreateDto.getUid())) throw new UidNotMatchedException("요청 UID와 인증 UID가 일치하지 않습니다.");
        Optional<UnLike> unlike = unLikeRepository.findByPostAndMember(post, member);
        Optional<Like> like = likeRepository.findByPostAndMember(post, member); 
        unlike.ifPresentOrElse(
            postLike -> { //싫어요가 있을 경우
                unLikeRepository.delete(postLike); //싫어요 데이터 삭제
                post.decreaseUnLikeCount(); //싫어요 카운트값 감소
            },
            () -> { //싫어요가 없을 경우
                like.ifPresentOrElse(
                    postLike -> { //좋아요가 있을 경우
                        post.decreaseLikeCount(); //좋아요 카운트값 감소
                        likeRepository.delete(postLike); //좋아요 데이터 삭제
                        post.increaseUnLikeCount(); //싫어요 카운트값 증가
                        unLikeRepository.save(new UnLike(member, post)); //싫어요 데이터 추가
                    },
                    () -> { //좋아요가 없을 경우
                        post.increaseUnLikeCount(); //싫어요 카운트값 증가
                        unLikeRepository.save(new UnLike(member, post)); //싫어요 데이터 추가
                    } 
                );
            } 
        );

        return unlike;
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
