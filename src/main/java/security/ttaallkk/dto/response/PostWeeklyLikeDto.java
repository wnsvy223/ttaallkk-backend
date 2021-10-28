package security.ttaallkk.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import security.ttaallkk.domain.post.Post;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostWeeklyLikeDto {

    private Long id;

    private String title;

    private Integer likeCnt;

    private LocalDateTime createdAt;

    private String email;

    private String uid;

    private String displayName;

    private String profileUrl;

    private String categoryName;

    // Dto변환
    public static List<PostWeeklyLikeDto> convertPostWeeklyLikeDto(List<Post> posts) {
        List<PostWeeklyLikeDto> result = posts
            .stream()
            .map(post -> new PostWeeklyLikeDto(
                post.getId(),
                post.getTitle(),
                post.getLikeCnt(),
                post.getCreatedAt(),
                post.getWriter().getUid(),
                post.getWriter().getEmail(),
                post.getWriter().getDisplayName(),
                post.getWriter().getProfileUrl(),
                post.getCategory().getCtgName())
            ).collect(Collectors.toList());
        return result;
    }
}
