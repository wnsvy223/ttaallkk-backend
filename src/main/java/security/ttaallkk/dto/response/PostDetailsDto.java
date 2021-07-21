package security.ttaallkk.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import security.ttaallkk.domain.post.Post;
import security.ttaallkk.domain.post.PostStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostDetailsDto implements Serializable{
    
    private Long id;
    
    private String title;

    private String content;

    private Integer likeCnt;

    private Integer views;

    private PostStatus postStatus;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    private String email;

    private String displayName;

    private String profileUrl;

    //PostDetailsDto로 변환
    public static PostDetailsDto convertResponseDto(Post post) {
        return new PostDetailsDto(
            post.getId(),
            post.getTitle(),
            post.getContent(),
            post.getLikeCnt(),
            post.getViews(),
            post.getPostStatus(),
            post.getCreatedAt(),
            post.getModifiedAt(),
            post.getWriter().getEmail(),
            post.getWriter().getDisplayName(),
            post.getWriter().getProfileUrl()
        );
    }
}
