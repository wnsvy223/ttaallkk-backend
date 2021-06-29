package security.ttaallkk.controller.post;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import security.ttaallkk.domain.post.Post;
import security.ttaallkk.dto.request.PostCreateDto;
import security.ttaallkk.dto.response.Response;
import security.ttaallkk.service.post.PostService;

@Controller
@RequestMapping("/api/post")
@RequiredArgsConstructor
@Log4j2
public class PostController {

    private final PostService postService;
    
    /**
     * 게시글 생성
     * @param postCreateDto
     * @return Response
     */
    @PostMapping("/")
    public ResponseEntity<Response> createPost(@RequestBody PostCreateDto postCreateDto){
        postService.createPost(postCreateDto);

        Response response = Response.builder()
            .status(200)
            .message("게시글 작성 성공").build();
        return ResponseEntity.ok(response);
    }

    /**
     * uid로 게시글 조회
     * @param uid
     * @return List<Post>
     */
    @GetMapping("/{uid}")
    public ResponseEntity<List<Post>> search(@PathVariable("uid") String uid) {
        List<Post> result = postService.findPostByUid(uid);
        return ResponseEntity.ok(result);
    }

    /**
     * 모든 게시글 삭제
     * @return Response
     */
    @DeleteMapping("/all")
    public ResponseEntity<Response> deletePost(){
        postService.deleteAllPost();
        
        Response response = Response.builder()
            .status(200)
            .message("게시글 삭제 성공").build();
        return ResponseEntity.ok(response);
    }
}
