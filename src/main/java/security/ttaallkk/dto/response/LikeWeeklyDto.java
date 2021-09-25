package security.ttaallkk.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import security.ttaallkk.domain.post.Like;

@Getter
@NoArgsConstructor
@AllArgsConstructor
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

    // LikeEntity -> LikeWeeklyDto 변환함수(feat. Map)
    public static List<LikeWeeklyDto> convertLikeEntityToLikeWeeklyDto(List<Like> likes) {
        List<LikeWeeklyDto> result = likes.stream()
                .map(like -> new LikeWeeklyDto(
                    like.getId(),
                    like.getPost().getId(),
                    like.getPost().getTitle(),
                    like.getPost().getLikeCnt(),
                    like.getPost().getCreatedAt(),
                    like.getMember().getEmail(),
                    like.getMember().getUid(),
                    like.getMember().getDisplayName(),
                    like.getMember().getProfileUrl())
                ).collect(Collectors.toList());
        return result;
    }
}
