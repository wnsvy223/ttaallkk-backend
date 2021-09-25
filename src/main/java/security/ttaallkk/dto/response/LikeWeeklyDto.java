package security.ttaallkk.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.querydsl.core.annotations.QueryProjection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import security.ttaallkk.domain.post.Like;

@Getter
@NoArgsConstructor
//@AllArgsConstructor
public class LikeWeeklyDto {
    private Long id;

    private Long postId;

    private String title;

    private Integer likeCnt;

    private LocalDateTime createdAt;

    private String email;

    private String uid;

    private String displayName;

    private String profileUrl;

    @QueryProjection
    public LikeWeeklyDto(
                Long id,
                Long postId,
                String title,
                Integer likeCnt,
                LocalDateTime createdAt,
                String email,
                String uid, 
                String displayName,
                String profileUrl) {

        this.id = id;
        this.postId = postId;
        this.title = title;
        this.likeCnt = likeCnt;
        this.createdAt = createdAt;
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.profileUrl = profileUrl;
    }
}
