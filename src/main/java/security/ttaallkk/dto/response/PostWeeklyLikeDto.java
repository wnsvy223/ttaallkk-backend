package security.ttaallkk.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import security.ttaallkk.common.Constant;
import security.ttaallkk.domain.post.Post;
import security.ttaallkk.domain.post.PostStatus;

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

    private String categoryTag;

    // Dto변환
    public static List<PostWeeklyLikeDto> convertPostWeeklyLikeDto(List<Post> posts) {
        List<PostWeeklyLikeDto> result = posts
            .stream()
            .map(post -> new PostWeeklyLikeDto(
                post.getId(),
                post.getPostStatus() == PostStatus.REMOVED ? Constant.POST_REMOVED_STATUS_MESSAGE : post.getTitle(),
                post.getLikeCnt(),
                post.getCreatedAt(),
                post.getWriter().getUid(),
                post.getWriter().getEmail(),
                post.getWriter().getDisplayName(),
                post.getWriter().getProfileUrl(),
                post.getCategory().getCtgName(),
                post.getCategory().getCtgTag())
            ).collect(Collectors.toList());
        return result;
    }
}
