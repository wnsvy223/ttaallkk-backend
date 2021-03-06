package security.ttaallkk.dto.querydsl;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import security.ttaallkk.common.Constant;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class CommentCommonDto {
    private Long id;

    private String content;

    private LocalDateTime createdAt;

    private Boolean isDeleted;

    private Long postId;

    private String categoryTag;

    private String categoryName;

    private String uid;

    private String email;

    private String displayName;

    private String profileUrl;

    @QueryProjection
    public CommentCommonDto(
                Long id,
                String content,
                LocalDateTime createdAt,
                Boolean isDeleted,
                Long postId,
                String categoryTag,
                String categoryName,
                String uid, 
                String email,
                String displayName,
                String profileUrl) {

        this.id = id;
        this.content = content;
        this.createdAt = createdAt;
        this.isDeleted = isDeleted;
        this.postId = postId;
        this.categoryTag = categoryTag;
        this.categoryName = categoryName;
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.profileUrl = profileUrl;
    }

    // 댓글 삭제상태에 따라 댓글 내용값 변경된 Dto로 변환
    public static List<CommentCommonDto> convertCommentCommonDto(List<CommentCommonDto> comments) {
        return comments
            .stream()
            .map(comment -> new CommentCommonDto(
                    comment.getId(),
                    comment.getIsDeleted() ? Constant.COMMENT_REMOVED_STATUS_MESSAGE : comment.getContent(),
                    comment.getCreatedAt(),
                    comment.getIsDeleted(),
                    comment.getPostId(),
                    comment.getCategoryTag(),
                    comment.getCategoryName(),
                    comment.getUid(),
                    comment.getEmail(),
                    comment.getDisplayName(),
                    comment.getProfileUrl()
                )
            ).collect(Collectors.toList());
    }
}
