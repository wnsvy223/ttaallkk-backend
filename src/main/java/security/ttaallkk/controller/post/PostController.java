package security.ttaallkk.controller.post;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import security.ttaallkk.domain.post.Like;
import security.ttaallkk.dto.querydsl.PostCommonDto;
import security.ttaallkk.dto.request.LikeCreateDto;
import security.ttaallkk.dto.request.PostCreateDto;
import security.ttaallkk.dto.request.PostUpdateDto;
import security.ttaallkk.dto.response.PostDetailResponseDto;
import security.ttaallkk.dto.response.Response;
import security.ttaallkk.service.post.PostSearchService;
import security.ttaallkk.service.post.PostService;

@Controller
@RequestMapping("/api/post")
@RequiredArgsConstructor
@Log4j2
public class PostController {

    private final PostService postService;
    private final PostSearchService postSearchService;
    
    /**
     * 게시글 생성
     * @param postCreateDto
     * @return Response
     */
    @PostMapping("/")
    public ResponseEntity<Response> createPost(@RequestBody PostCreateDto postCreateDto) {
        postService.createPost(postCreateDto);

        Response response = Response.builder()
            .status(200)
            .message("게시글 작성 성공").build();
        return ResponseEntity.ok(response);
    }

    /**
     * 게시글 데이터 조회
     * @param postId
     * @return PostWithCommentsResponseDto
     */
    @GetMapping("/{postId}")
    public ResponseEntity<PostDetailResponseDto> getPostDetailsWithComments(@PathVariable("postId") Long postId) {
        PostDetailResponseDto result = postService.findPostByPostIdWithComments(postId);
        return ResponseEntity.ok(result);
    }

    /**
     * 게시글 수정
     * @param postUpdateDto
     * @return
     */
    @PutMapping("/{postId}")
    public ResponseEntity<Response> updatePost(
                @RequestBody PostUpdateDto postUpdateDto, 
                @PathVariable("postId") Long postId) {

        Response response = postService.updatePost(postUpdateDto, postId);
        
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    /**
     * 게시글 삭제(단일 삭제)
     * @param postId
     * @return Response
     */
    @DeleteMapping("/{postId}")
    public ResponseEntity<Response> deletePost(@PathVariable("postId") Long postId) {
        Response response = postService.deletePost(postId);
        
        return ResponseEntity
                .status(response.getStatus())
                .body(response);
    }

    /**
     * 메인 페이지 게시글 페이징 최신 게시글 15개 조회
     * @return List<PostWithMemberDto>
     */
    @GetMapping("/")
    public ResponseEntity<List<PostCommonDto>> getPostsForPreView() {
        List<PostCommonDto> result = postService.findPostByRecent(15);
        return ResponseEntity.ok(result);
    }

    /**
     * 페이징
     * @param page
     * @param pageable
     * @return Page<PostWithMemberDto>
     */
    @GetMapping
    public ResponseEntity<Page<PostCommonDto>> getPostsByPageNumber(
                @RequestParam(value = "page", defaultValue = "0") int page, 
                @PageableDefault(size = 20) Pageable pageable) {

        Page<PostCommonDto> result = postService.paging(pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * 게시글 Full Text Search + Paging
     * @param keyword
     * @param page
     * @param pageable
     * @return Page<Post>
     */
    @GetMapping("/search")
    public ResponseEntity<Page<PostCommonDto>> searchPost(
                @RequestParam(value = "keyword") String keyword,
                @RequestParam(value = "page", defaultValue = "0") int page,
                @PageableDefault(size = 20) Pageable pageable) {

        Page<PostCommonDto> result = postSearchService.searchPostByTitleOrContent(keyword, pageable);
        return ResponseEntity.ok(result);
    }

    /**
     * 작성자 uid로 게시글 조회
     * @param uid
     * @return List<PostByMemberDto>
     */
    @GetMapping("/user/{uid}")
    public ResponseEntity<List<PostCommonDto>> getPostsByWriterUid(@PathVariable("uid") String uid) {
        List<PostCommonDto> result = postService.findPostByUid(uid);
        return ResponseEntity.ok(result);
    }

    /**
     * 게시글 삭제(전체 삭제) - 관리자만 전체 삭제 가능
     * @return Response
     */
    @DeleteMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Response> deletePost() {
        postService.deleteAllPost();
        
        Response response = Response.builder()
            .status(200)
            .message("게시글 전체 삭제 성공").build();
        return ResponseEntity.ok(response);
    }

    /**
     * 좋아요 등록
     * @param likeCreateDto //게시글 아이디 + 좋아요 요청사용자 Uid
     * @return Response
     */
    @PostMapping("/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response> createLike(@RequestBody LikeCreateDto likeCreateDto) {
        Optional<Like> like = postService.createLike(likeCreateDto);
        if(like.isPresent()){
            Response response = Response.builder()
                .status(200)
                .message("좋아요 취소").build();
            return ResponseEntity.ok(response);
        }else{
            Response response = Response.builder()
                .status(200)
                .message("좋아요 등록").build();
            return ResponseEntity.ok(response);
        }
    }

}
