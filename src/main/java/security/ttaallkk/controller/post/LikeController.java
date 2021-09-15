package security.ttaallkk.controller.post;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import security.ttaallkk.domain.post.Like;
import security.ttaallkk.dto.querydsl.LikeCommonDto;
import security.ttaallkk.dto.request.LikeCreateDto;
import security.ttaallkk.dto.response.Response;
import security.ttaallkk.service.post.LikeService;

@Controller
@RequestMapping("/api/like")
@RequiredArgsConstructor
@Log4j2
public class LikeController {
    
    private final LikeService likeService;

     /**
     * 좋아요 등록
     * @param likeCreateDto //게시글 아이디 + 좋아요 요청사용자 Uid
     * @return Response
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response> createLike(@RequestBody LikeCreateDto likeCreateDto) {
        Optional<Like> like = likeService.createLike(likeCreateDto);
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

    /**
     * 로그인한 유저가 좋아요 누른 게시글 uid 기반으로 조회
     * @param uid
     * @return
     */
    @GetMapping("/{uid}")
    public ResponseEntity<List<LikeCommonDto>> getLikePostByUid(@PathVariable("uid") String uid) {
        List<LikeCommonDto> likes = likeService.getMyLikePost(uid);
        return ResponseEntity.ok(likes);
    }
}
