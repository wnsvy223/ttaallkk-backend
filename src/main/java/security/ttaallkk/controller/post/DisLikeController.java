package security.ttaallkk.controller.post;

import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import security.ttaallkk.domain.post.DisLike;
import security.ttaallkk.dto.request.DisLikeCreateDto;
import security.ttaallkk.dto.response.Response;
import security.ttaallkk.service.post.DisLikeService;

@Controller
@RequestMapping("/api/dislike")
@RequiredArgsConstructor
public class DisLikeController {
    
    private final DisLikeService disLikeService;

    /**
     * 싫어요 등록
     * @param dislikeCreateDto
     * @return Response
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response> createLike(@RequestBody DisLikeCreateDto dislikeCreateDto) {
        Optional<DisLike> dislike = disLikeService.createDisLike(dislikeCreateDto);
        if(dislike.isPresent()){
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
