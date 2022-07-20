package security.ttaallkk.dto.querydsl;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;

@Getter
public class LikeCommonDto {
    
    private Long id;

    private Long postId;

    private LocalDateTime createdAt;

    private Integer likeCnt;

    private String title;

    private String categoryTag;
    
    private String categoryName;

    private String uid;

    private String email;

    private String displayName;

    private String profileUrl;

    @QueryProjection
    public LikeCommonDto(
                Long id,
                Long postId,
                LocalDateTime createdAt,
                Integer likeCnt,
                String title,
                String categoryTag,
                String categoryName,
                String uid,
                String email,
                String displayName,
                String profileUrl) {

        this.id = id;
        this.postId = postId;
        this.createdAt = createdAt;
        this.likeCnt = likeCnt;
        this.title = title;
        this.categoryTag = categoryTag;
        this.categoryName = categoryName;
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.profileUrl = profileUrl;
    }
}
