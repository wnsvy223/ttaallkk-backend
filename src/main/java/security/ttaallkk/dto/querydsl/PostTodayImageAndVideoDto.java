package security.ttaallkk.dto.querydsl;

import java.time.LocalDateTime;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Getter;



@Getter
public class PostTodayImageAndVideoDto {

    private Long id;

    private String title;

    private String content;

    private LocalDateTime createdAt;

    private String email;

    private String uid;

    private String displayName;

    private String profileUrl;

    private String categoryName;

    private String categoryTag;

    @QueryProjection
    public PostTodayImageAndVideoDto(
                Long id,
                String title, 
                String content, 
                LocalDateTime createdAt,
                String email,
                String uid,
                String displayName,
                String profileUrl,
                String categoryName,
                String categoryTag) {

        this.id = id;
        this.title = title;
        this.content = content;
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.profileUrl = profileUrl;
        this.createdAt = createdAt;
        this.categoryName = categoryName;
        this.categoryTag = categoryTag;
    }
}
