package security.ttaallkk.controller.post;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import security.ttaallkk.dto.request.CommentCreateDto;
import security.ttaallkk.dto.request.CommentUpdateDto;
import security.ttaallkk.dto.response.CommentResponseDto;
import security.ttaallkk.dto.response.Response;
import security.ttaallkk.service.post.CommentService;


@Controller
@RequestMapping("/api/comment")
@RequiredArgsConstructor
@Log4j2
public class CommentController {

    private final CommentService commentService;
    
    /**
     * 댓글 생성
     * @param commentCreateDto
     * @return Response
     */
    @PostMapping("/")
    public ResponseEntity<Response> createComment(@RequestBody CommentCreateDto commentCreateDto) {
        
        commentService.createComment(commentCreateDto);
        
        Response response = Response.builder()
            .status(200)
            .message("댓글 작성 성공").build();
        return ResponseEntity.ok(response);
    }

    /**
     * 댓글 조회(해당 번호의 게시글에 연관된 댓글을 계층형 댓글로 조회)
     * @param postId
     * @return List<CommentResponseDto>
     */
    @GetMapping("/{postId}")
    public ResponseEntity<List<CommentResponseDto>> getCommentByPostId(@PathVariable("postId") Long postId){
        
        List<CommentResponseDto> result = commentService.findCommentByPostId(postId);

        return ResponseEntity.ok(result);
    }

    /**
     * 댓글 변경
     * @param commentUpdateDto
     * @param commentId
     * @return Response
     */
    @PutMapping("/{commentId}")
    public ResponseEntity<Response> updateComment(
                @RequestBody CommentUpdateDto commentUpdateDto,
                @PathVariable("commentId") Long commentId) {

        commentService.updateCommentContent(commentUpdateDto, commentId);

        Response response = Response.builder()
            .status(200)
            .message("댓글 작성 성공").build();
        return ResponseEntity.ok(response);
    }

    /**
     * 댓글 삭제(댓글 계층구조에서 부모 및 자식관계에 따라 삭제 또는 삭제상태로 변경처리)
     * @param commentId
     * @return Response
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Response> deleteComment(@PathVariable("commentId") Long commentId) {
        
        commentService.deleteComment(commentId);

        Response response = Response.builder()
            .status(200)
            .message("댓글 삭제 성공").build();
        return ResponseEntity.ok(response);
    }
}