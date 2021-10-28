package security.ttaallkk.service.post;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import security.ttaallkk.domain.member.Member;
import security.ttaallkk.domain.post.Category;
import security.ttaallkk.domain.post.Like;
import security.ttaallkk.domain.post.Post;
import security.ttaallkk.domain.post.PostStatus;
import security.ttaallkk.dto.querydsl.PostCommonDto;
import security.ttaallkk.dto.request.PostCreateDto;
import security.ttaallkk.dto.request.PostUpdateDto;
import security.ttaallkk.dto.response.CommentResponseDto;
import security.ttaallkk.dto.response.PostDetailResponseDto;
import security.ttaallkk.dto.response.Response;
import security.ttaallkk.exception.CategoryNotFoundException;
import security.ttaallkk.exception.PostNotFoundException;
import security.ttaallkk.exception.UidNotFoundException;
import security.ttaallkk.repository.member.MemberRepository;
import security.ttaallkk.repository.post.CategoryRepository;
import security.ttaallkk.repository.post.LikeRepository;
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
    private final LikeRepository likeRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 게시글 생성
     * @param postCreateDto
     * @return Post : 생성된 게시글 정보
     */
    @Transactional
    public Post createPost(PostCreateDto postCreateDto) {        
        Member member = memberRepository.findMemberByUid(postCreateDto.getWriteUid())
            .orElseThrow(() -> new UidNotFoundException("존재하지 않는 Uid입니다."));

        Category category = categoryRepository.findById(postCreateDto.getCategoryId())
            .orElseThrow(() -> new CategoryNotFoundException("존재하지 않는 카테고리 입니다."));
        
        Post post = Post.builder()
            .writer(member)
            .title(postCreateDto.getTitle())
            .content(postCreateDto.getContent())
            .postStatus(PostStatus.NORMAL)
            .category(category)
            .views(0)
            .likeCnt(0)
            .build();

        return postRepository.save(post);
    }

    /**
     * 게시글내용 + 댓글(계층형 구조로 반환) + 좋아요(인증된 사용자 정보를 기반으로 좋아요 등록 유무값으로 반환) 조회하여 반환 
     * @param postId
     * @return PostDetailResponseDto
     */
    @Transactional
    public PostDetailResponseDto findPostForDetail(Long postId) { 
        Post post = postRepository.findPostByPostId(postId).orElseThrow(PostNotFoundException::new); //게시글 데이터를 조회
        List<CommentResponseDto> comments = CommentResponseDto.convertCommentStructure(post.getComments()); //게시글에 연관된 댓글데이터를 가져와서 계층형 댓글구조로 변환
        Boolean isLike = isAlreadyLikeWithAuthUser(post); //인증된 사용자의 좋아요 유무 체크
        if(isNormalPermissionAuthUser() == true) {
            post.updateViewsCount();
        }
        PostDetailResponseDto postDetailResponseDto = PostDetailResponseDto.builder() //PostDetailResponseDto생성하여 데이터 세팅 후 반환
            .id(post.getId())
            .title(post.getTitle())
            .content(post.getContent())
            .likeCnt(post.getLikeCnt())
            .views(post.getViews())
            .postStatus(post.getPostStatus())
            .createdAt(post.getCreatedAt())
            .modifiedAt(post.getModifiedAt())
            .category(post.getCategory())
            .email(post.getWriter().getEmail())
            .displayName(post.getWriter().getDisplayName())
            .uid(post.getWriter().getUid())
            .profileUrl(post.getWriter().getProfileUrl())
            .comments(comments)
            .isAlreadyLike(isLike)
            .build();
        return postDetailResponseDto;
    }

    /**
     * 인증정보에 담긴 이메일과 요청값으로 넘어오는 이메일값을 비교하여 본인 권한 체크
     * @param email
     * @return Boolean
     */
    private Boolean isOwnerPermissionAuthUser(String email) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication.getName().equals(email)) {
            return true;
        }else{
            return false;
        }
    }

    /**
     * 인증된 사용자의 좋아요 유무 체크
     * 인증O 좋아요 데이터 존재O -> true
     * 인증O 좋아요 데이터 존재X -> false
     * 인증X 사용자 -> false
     * @param post
     * @return Boolean
     */
    private Boolean isAlreadyLikeWithAuthUser(Post post) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Member member = memberRepository.findMemberByEmail(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException("이메일이 일치하지 않습니다."));
            Optional<Like> like = likeRepository.findByPostAndMember(post, member);
            return like.isPresent();
        }else{
            return false;
        }
    }

    /**
     * 사용자 권한 체크 : 관리자(false) , 일반or익명유저(true) 반환 -> 관리자 계정은 조회수를 증가시키지않음
     * @return Boolean
     */
    private Boolean isNormalPermissionAuthUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_USER") || a.getAuthority().equals("ROLE_ANONYMOUS"))){
            return true;
        }else{
            return false;
        }
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

        //현재 인증된 사용자의 이메일이 게시글 작성자 이메일과 같을 경우에만 수정 가능
        if(isOwnerPermissionAuthUser(post.getWriter().getEmail())){
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

        //현재 인증된 사용자의 이메일이 게시글 작성자 이메일과 같을 경우에만 삭제 가능
        if(isOwnerPermissionAuthUser(post.getWriter().getEmail())){
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
     * @return List<PostCommonDto> : 조회된 게시글의 작성자 정보를 포함한 목록
     */
    public List<PostCommonDto> findPostByRecent() {
        List<PostCommonDto> result = postRepositorySupport.findPostByRecent();

        return result;
    }

    /**
     * 페이징
     * @param pageable
     * @return Page<PostCommonDto> : 페이징정보 + 조회된 게시글의 작성자 정보를 포함한 목록
     */
    public Page<PostCommonDto> paging(Pageable pageable, Long categoryId) {
        Page<PostCommonDto> result = postRepositorySupport.paging(pageable, categoryId);

        return result;
    }

    /**
     * 해당 uid의 사용자가 작성한 게시글 조회
     * @param uid
     * @return List<PostCommonDto> : 조회된 게시글의 작성자 정보를 포함한 목록
     */
    public List<PostCommonDto> findPostByUid(String uid) {
        List<PostCommonDto> result = postRepositorySupport.findPostByUid(uid);

        return result;
    }

    /**
     * 주간 좋아요를 받은 숫자가 높은 순서대로 조회하여 반환
     * @return List<PostWeeklyLikeDto>
     */
    @Transactional
    public List<Post> findPostWeeklyLike() {
        // 저번주 일요일 + 1 = 이번주 월요일
        LocalDateTime from = LocalDateTime.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY.plus(1)));
        // 다음주 월요일 -1 = 이번주 일요일
        LocalDateTime to = LocalDateTime.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY.minus(1)));
        // 이번주 월요일 ~ 일요일까지의 주간 범위값 전달
        List<Post> result = postRepositorySupport.findPostByWeeklyLike(from, to);

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
