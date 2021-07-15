package security.ttaallkk.dto.querydsl;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import security.ttaallkk.domain.post.PostStatus;


@Getter
public class PostWithMemberDto{
    
    private String email;

    private String displayName;

    private String profileUrl;

    private Long id;
    
    private String title;

    private String content;

    private PostStatus postStatus;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    @QueryProjection
    public PostWithMemberDto(
                String email, 
                String displayName, 
                String profileUrl, 
                Long id,
                String title, 
                String content, 
                PostStatus postStatus,
                LocalDateTime createdAt,
                LocalDateTime modifiedAt) {

        this.email = email;
        this.displayName = displayName;
        this.profileUrl = profileUrl;
        this.id = id;
        this.title = title;
        this.content = content;
        this.postStatus = postStatus;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }
}
