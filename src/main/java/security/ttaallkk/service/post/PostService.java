package security.ttaallkk.service.post;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import security.ttaallkk.domain.member.Member;
import security.ttaallkk.domain.post.Post;
import security.ttaallkk.domain.post.PostStatus;
import security.ttaallkk.dto.querydsl.PostWithMemberDto;
import security.ttaallkk.dto.request.PostCreateDto;
import security.ttaallkk.dto.request.PostUpdateDto;
import security.ttaallkk.dto.response.CommentResponseDto;
import security.ttaallkk.dto.response.PostDetailsDto;
import security.ttaallkk.dto.response.PostWithCommentsResponseDto;
import security.ttaallkk.dto.response.Response;
import security.ttaallkk.exception.PostNotFoundException;
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
    public Post createPost(PostCreateDto postCreateDto) {        
        Member member = memberRepository.findMemberByUid(postCreateDto.getWriteUid())
            .orElseThrow(() -> new UidNotFoundException("존재하지 않는 Uid입니다."));
        
        Post post = Post.builder()
            .writer(member)
            .title(postCreateDto.getTitle())
            .content(postCreateDto.getContent())
            .postStatus(PostStatus.NORMAL)
            .views(0)
            .likeCnt(0)
            .build();

        return postRepository.save(post);
    }

    /**
     * 게시글 내용보기
     * @param postId
     * @return PostDetailsDto : 게시글 상세내용 정보
     */
    public PostDetailsDto findPostByPostId(Long postId) {
        Post post = postRepository.findPostByPostId(postId).orElseThrow(PostNotFoundException::new);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        //일반유저 또는 익명유저의 경우에만 조회수 증가(관리자는 조회수 증가 X)
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER") || a.getAuthority().equals("ROLE_ANONYMOUS"))){
            post.updateViewsCount(); //조회수 증가(업데이트는 어플리케이션 레벨에서 수행하는 것을 권장)
        }
        PostDetailsDto result = PostDetailsDto.convertResponseDto(post);
        
        return result;
    }

    /**
     * 게시글과 게시글에 연관된 계층댓글 함께 조회
     * @param postId
     * @return PostWithCommentsResponseDto
     */
    public PostWithCommentsResponseDto findPostByPostIdWithComments(Long postId) { 
        Post post = postRepository.findPostByPostId(postId).orElseThrow(PostNotFoundException::new); //게시글 데이터를 조회
        List<CommentResponseDto> comments = CommentResponseDto.convertCommentStructure(post.getComments()); //게시글에 연관된 댓글데이터를 가져와서 계층형 댓글구조로 변환
        PostWithCommentsResponseDto postWithCommentsDto = PostWithCommentsResponseDto.builder() //PostWithCommentsResponseDto생성하여 데이터 세팅 후 반환
            .post(post)
            .comments(comments)
            .build();
        return postWithCommentsDto;
    }

    /**
     * 게시글 수정
     * @param postUpdateDto
     * @param postId
     * @return Response
     */
    @Transactional
    public Response updatePost(PostUpdateDto postUpdateDto, Long postId) {
        Post post = postRepository.findPostByPostId(postId).orElseThrow(PostNotFoundException::new);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //현재 인증된 사용자의 이메일이 게시글 작성자 이메일과 같을 경우에만 수정 가능
        if(post.getWriter().getEmail().equals(authentication.getName())){
            post.updatePost(
                postUpdateDto.getTitle(), 
                postUpdateDto.getContent()
            );
            return Response.builder()
                .status(200)
                .message("게시글 수정 성공")
                .build();
        }else{
            return Response.builder()
                .status(403)
                .message("게시글을 수정할 수 있는 권한이 없습니다.")
                .build();
        }  
    }
    
    /**
     * 게시글 단일 삭제
     * @param postId
     * @return Response
     */
    @Transactional
    public Response deletePost(Long postId) {
        Post post = postRepository.findPostByPostId(postId).orElseThrow(PostNotFoundException::new);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        //현재 인증된 사용자의 이메일이 게시글 작성자 이메일과 같을 경우에만 삭제 가능
        if(post.getWriter().getEmail().equals(authentication.getName())){
            postRepository.deleteById(postId);
            return Response.builder()
                .status(200)
                .message("게시글 삭제 성공")
                .build();
        }else{
            return Response.builder()
                .status(403)
                .message("게시글을 삭제할 수 있는 권한이 없습니다.")
                .build();
        }  
    }

    /**
     * 최신 게시글 조회
     * @param limit
     * @return List<PostWithMemberDto> : 조회된 게시글의 작성자 정보를 포함한 목록
     */
    public List<PostWithMemberDto> findPostByRecent(int limit) {
        List<PostWithMemberDto> result = postRepositorySupport.findPostByRecent(limit);

        return result;
    }

    /**
     * 페이징
     * @param pageable
     * @return Page<PostWithMemberDto> : 페이징정보 + 조회된 게시글의 작성자 정보를 포함한 목록
     */
    public Page<PostWithMemberDto> paging(Pageable pageable) {
        Page<PostWithMemberDto> result = postRepositorySupport.paging(pageable);

        return result;
    }

    /**
     * 해당 uid의 사용자가 작성한 게시글 조회
     * @param uid
     * @return List<PostByMemberDto> : 조회된 게시글의 작성자 정보를 포함한 목록
     */
    public List<PostWithMemberDto> findPostByUid(String uid) {
        List<PostWithMemberDto> result = postRepositorySupport.findPostByUid(uid);

        return result;
    }

    /**
     * 게시글 전체 삭제
     */
    @Transactional
    public void deleteAllPost() {
        postRepository.deleteAll();
    }
}
