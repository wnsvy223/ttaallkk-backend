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
import security.ttaallkk.dto.querydsl.LikeCommonDto;
import security.ttaallkk.dto.request.LikeCreateDto;
import security.ttaallkk.exception.PostNotFoundException;
import security.ttaallkk.repository.member.MemberRepository;
import security.ttaallkk.repository.post.LikeRepository;
import security.ttaallkk.repository.post.LikeRepositorySupport;
import security.ttaallkk.repository.post.PostRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Log4j2
public class LikeService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final LikeRepository likeRepository;
    private final LikeRepositorySupport likeRepositorySupport;
 
     /**
     * 좋아요 등록 : 인증된 사용자가 게시글에 좋아요 등록. 이미 좋아요 등록한 게시글일 경우 좋아요 취소.
     * @param likeCreateDto
     * @return Optional<Like>
     */
    @Transactional
    public Optional<Like> createLike(LikeCreateDto likeCreateDto) {
        Post post = postRepository.findPostByPostId(likeCreateDto.getPostId()).orElseThrow(PostNotFoundException::new);
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findMemberByEmail(email).orElseThrow(() -> new UsernameNotFoundException("이메일이 일치하지 않습니다."));
        Optional<Like> like = likeRepository.findByPostAndMember(post, member);
        like.ifPresentOrElse(
            postLike -> { //좋아요가 있을 경우
                likeRepository.delete(postLike); //좋아요 데이터 삭제
                post.decreaseLikeCount(); //좋아요 카운트값 감소
            },
            () -> { //좋아요가 없을 경우
                likeRepository.save(new Like(member, post)); //좋아요 데이터 추가
                post.increaseLikeCount(); //좋아요 카운트값 증가
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
}
