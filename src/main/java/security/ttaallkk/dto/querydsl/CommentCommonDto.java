package security.ttaallkk.dto.querydsl;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentCommonDto {
    private Long id;

    private Long postId;

    private String content;

    private LocalDateTime createdAt;

    private String uid;

    private String email;

    private String displayName;

    private String profileUrl;

    @QueryProjection
    public CommentCommonDto(
                Long id,
                String content,
                LocalDateTime createdAt,
                Long postId,
                String uid, 
                String email,
                String displayName,
                String profileUrl) {

        this.id = id;
        this.postId = postId;
        this.content = content;
        this.createdAt = createdAt;
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.profileUrl = profileUrl;
    }
}
