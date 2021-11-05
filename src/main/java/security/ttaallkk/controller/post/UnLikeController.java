package security.ttaallkk.controller.post;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import security.ttaallkk.domain.post.UnLike;
import security.ttaallkk.dto.request.UnLikeCreateDto;
import security.ttaallkk.dto.response.Response;
import security.ttaallkk.service.post.UnLikeService;

@Controller
@RequestMapping("/api/unlike")
@RequiredArgsConstructor
public class UnLikeController {
    
    private final UnLikeService unLikeService;

    /**
     * 싫어요 등록
     * @param unlikeCreateDto
     * @return Response
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response> createLike(@RequestBody UnLikeCreateDto unlikeCreateDto) {
        Optional<UnLike> like = unLikeService.createUnLike(unlikeCreateDto);
        if(like.isPresent()){
            Response response = Response.builder()
                .status(200)
                .message("싫어요 취소").build();
            return ResponseEntity.ok(response);
        }else{
            Response response = Response.builder()
                .status(200)
                .message("싫어요 등록").build();
            return ResponseEntity.ok(response);
        }
    }
}
