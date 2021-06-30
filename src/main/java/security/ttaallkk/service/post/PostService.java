package security.ttaallkk.service.post;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import security.ttaallkk.domain.member.Member;
import security.ttaallkk.domain.post.Post;
import security.ttaallkk.dto.request.PostCreateDto;
import security.ttaallkk.exception.UidNotFoundException;
import security.ttaallkk.repository.member.MemberRepository;
import security.ttaallkk.repository.post.PostRepository;
import security.ttaallkk.repository.post.PostRepositorySupport;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Log4j2
public class PostService {
 
    private final PostRepository postRepository; //JPA Repository
    private final PostRepositorySupport postRepositorySupport; //Query DSL Repository
    private final MemberRepository memberRepository;

    /**
     * 게시글 생성
     * @param postCreateDto
     * @return Post : 생성된 게시글 정보
     */
    @Transactional
    public Post createPost(PostCreateDto postCreateDto){
        
        Member member = memberRepository.findMemberByUid(postCreateDto.getWriteUid())
            .orElseThrow(() -> new UidNotFoundException("존재하지 않는 Uid입니다."));
        
        Post post = Post.builder()
            .writer(member)
            .title(postCreateDto.getTitle())
            .content(postCreateDto.getContent())
            .build();

        return postRepository.save(post);
    }

    /**
     * 해당 uid의 사용자가 작성한 게시글 조회
     * @param uid
     * @return List<Post> 조회된 게시글 목록
     */
    @Transactional
    public List<Post> findPostByUid(String uid){
        List<Post> result = postRepository.findPostByWriterUid(uid);

        return result;
    }

    /**
     * 모든 게시글 삭제
     */
    @Transactional
    public void deleteAllPost(){
        postRepository.deleteAll();
    }
}
