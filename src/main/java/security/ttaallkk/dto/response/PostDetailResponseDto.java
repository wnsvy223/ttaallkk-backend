package security.ttaallkk.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import security.ttaallkk.domain.post.Category;
import security.ttaallkk.domain.post.PostStatus;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDetailResponseDto {
    
    //Post;
    private Long id;
    
    private String title;

    private String content;
    
    private Integer commentCnt;

    private Integer likeCnt;

    private Integer unLikeCnt;

    private Integer views;

    private PostStatus postStatus;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    private Category category;

    //Member
    private String email;

    private String displayName;

    private String uid;

    private String profileUrl;

    //Comments
    @Builder.Default
    private List<CommentResponseDto> comments = new ArrayList<>();

    //Like
    private Boolean isAlreadyLike;

    //UnLike
    private Boolean isAlreadyUnLike;
}
