package security.ttaallkk.service.post;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import security.ttaallkk.common.Constant;
import security.ttaallkk.common.authentication.AuthenticationHelper;
import security.ttaallkk.domain.member.Member;
import security.ttaallkk.domain.post.Category;
import security.ttaallkk.domain.post.Like;
import security.ttaallkk.domain.post.DisLike;
import security.ttaallkk.domain.post.Post;
import security.ttaallkk.domain.post.PostStatus;
import security.ttaallkk.dto.querydsl.PostCommonDto;
import security.ttaallkk.dto.request.PostCreateDto;
import security.ttaallkk.dto.request.PostUpdateDto;
import security.ttaallkk.dto.response.PostDetailResponseDto;
import security.ttaallkk.dto.response.PostWeeklyLikeDto;
import security.ttaallkk.dto.response.Response;
import security.ttaallkk.exception.CategoryNotFoundException;
import security.ttaallkk.exception.PermissionDeniedException;
import security.ttaallkk.exception.PostAlreadyRemovedException;
import security.ttaallkk.exception.PostNotFoundException;
import security.ttaallkk.exception.UidNotFoundException;
import security.ttaallkk.repository.member.MemberRepository;
import security.ttaallkk.repository.post.CategoryRepository;
import security.ttaallkk.repository.post.LikeRepository;
import security.ttaallkk.repository.post.DisLikeRepository;
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
    private final DisLikeRepository disLikeRepository;
    private final CategoryRepository categoryRepository;
    private final AuthenticationHelper authenticationHelper;

    /**
     * 게시글 생성
     * @param postCreateDto
     * @return Post : 생성된 게시글 정보
     */
    @Transactional
    public Post createPost(PostCreateDto postCreateDto) {        
        Member member = memberRepository.findMemberByUid(postCreateDto.getWriteUid()).orElseThrow(UidNotFoundException::new);
        Category category = categoryRepository.findById(postCreateDto.getCategoryId()).orElseThrow(CategoryNotFoundException::new);
    
        Post post = Post.builder()
            .writer(member)
            .title(postCreateDto.getTitle())
            .content(postCreateDto.getContent())
            .postStatus(PostStatus.NORMAL)
            .category(category)
            .views(0)
            .likeCnt(0)
            .dislikeCnt(0)
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
        Boolean isLike = isCurrentUserAlreadyLike(post); //인증된 사용자의 좋아요 유무 체크
        Boolean isDisLike = isCurrentUserAlreadyDisLike(post); //인증된 사용자의 싫어요 유무 체크
        if(authenticationHelper.isNormalOrAnonymousUser()) {
            post.updateViewsCount();
        }
        return PostDetailResponseDto.convertPostDetailResponseDto(post, isLike, isDisLike);
    }

    /**
     * 게시글 수정
     * @param postUpdateDto
     * @param postId
     * @return PostDetailResponseDto
     * @exception PostNotFoundException //게시글을 찾을 수 없음
     * @exception PermissionDeniedException //권한 없음
     */
    @Transactional
    public PostDetailResponseDto updatePost(PostUpdateDto postUpdateDto, Long postId) {
        Post post = postRepository.findPostByPostId(postId).orElseThrow(PostNotFoundException::new);
        Boolean isLike = isCurrentUserAlreadyLike(post); //인증된 사용자의 좋아요 유무 체크
        Boolean isDisLike = isCurrentUserAlreadyDisLike(post); //인증된 사용자의 싫어요 유무 체크
        if(authenticationHelper.isNormalOrAnonymousUser()) {
            post.updateViewsCount();
        }
        //현재 인증된 사용자의 이메일이 게시글 작성자 이메일과 같을 경우에만 수정 가능
        if(authenticationHelper.isOwnerEmail(post.getWriter().getEmail())) {
            if(post.getPostStatus() == PostStatus.REMOVED) { //이미 삭제된 게시글이면 오류 응답
                throw new PostAlreadyRemovedException();
            }else{
                post.updatePost(
                    postUpdateDto.getTitle(), 
                    postUpdateDto.getContent()
                );
                return PostDetailResponseDto.convertPostDetailResponseDto(post, isLike, isDisLike);
            }
        }else{
            throw new PermissionDeniedException(); //권한 오류
        }  
    }
    
    /**
     * 게시글 단일 삭제
     * @param postId
     * @return Response
     * @exception PostNotFoundException //게시글을 찾을 수 없음
     * @exception PermissionDeniedException //권한 없음
     */
    @Transactional
    public Response deletePost(Long postId) {
        Post post = postRepository.findPostByPostId(postId).orElseThrow(PostNotFoundException::new);

        //현재 인증된 사용자의 이메일이 게시글 작성자 이메일과 같을 경우에만 삭제 가능
        if(authenticationHelper.isOwnerEmail(post.getWriter().getEmail())) {
            if(post.getPostStatus() == PostStatus.REMOVED) { //이미 삭제된 게시글이면 오류 응답
                throw new PostAlreadyRemovedException();
            }else{
                post.updatePostStatusToDelete();
                return Response.builder()
                    .status(200)
                    .message(Constant.POST_REMOVE_SUCCESS)
                    .build();
            }
        }else{
            throw new PermissionDeniedException(); //권한 오류
        }  
    }

    /**
     * 최신 게시글 조회
     * @param limit
     * @return List<PostCommonDto> : 조회된 게시글의 작성자 정보를 포함한 목록
     */
    public List<PostCommonDto> findPostByRecent() {
        List<PostCommonDto> posts = postRepositorySupport.findPostByRecent();
        List<PostCommonDto> result = PostCommonDto.convertPostCommonDtoElement(posts);
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
        List<PostCommonDto> posts = postRepositorySupport.findPostByUid(uid);
        List<PostCommonDto> result = PostCommonDto.convertPostCommonDtoElement(posts);

        return result;
    }

    /**
     * 주간 좋아요를 받은 숫자가 높은 순서대로 조회하여 반환
     * @return List<PostWeeklyLikeDto>
     */
    @Transactional
    public List<PostWeeklyLikeDto> findPostWeeklyLike() {
        // 저번주 일요일 + 1 = 이번주 월요일 0시 0분 0초
        LocalDateTime from = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY.plus(1))).atTime(LocalTime.MIDNIGHT);
        // 다음주 월요일 -1 = 이번주 일요일 23시 59분 59초
        LocalDateTime to = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY.minus(1))).atTime(LocalTime.MAX);
        // 이번주 월요일 ~ 일요일까지의 주간 범위값 전달
        List<Post> posts = postRepositorySupport.findPostByWeeklyLike(from, to);
        List<PostWeeklyLikeDto> result = PostWeeklyLikeDto.convertPostWeeklyLikeDto(posts);
        
        return result;
    }

    /**
     * 게시글 전체 삭제
     */
    @Transactional
    public void deleteAllPost() {
        postRepository.deleteAll();
    }

    /**
     * 사용자의 좋아요 유무 체크
     * 쿠키에서 추출한 uid를 통해 좋아요 데이터를 조회 후 사용자의 좋아요 유무 상태를 반환
     * @param post
     * @return Boolean
     */
    private Boolean isCurrentUserAlreadyLike(Post post) {
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        Cookie cookie = WebUtils.getCookie(request, "uid");
        if (cookie != null) {
            String uid = cookie.getValue();
            Member member = memberRepository.findMemberByUid(uid).orElseThrow(UidNotFoundException::new);
            Optional<Like> like = likeRepository.findByPostAndMember(post, member);
            return like.isPresent();
        }else{
            return false;
        }
    }

    /**
     * 사용자의 싫어요 유무 체크
     * 쿠키에서 추출한 uid를 통해 싫어요 데이터를 조회 후 사용자의 싫어요 유무 상태를 반환
     * @param post
     * @return Boolean
     */
    private Boolean isCurrentUserAlreadyDisLike(Post post) {
        HttpServletRequest request = ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
        Cookie cookie = WebUtils.getCookie(request, "uid");
        if (cookie != null) {
            String uid = cookie.getValue();
            Member member = memberRepository.findMemberByUid(uid).orElseThrow(UidNotFoundException::new);
            Optional<DisLike> dislike = disLikeRepository.findByPostAndMember(post, member);
            return dislike.isPresent();
        }else{
            return false;
        }
    }

}
