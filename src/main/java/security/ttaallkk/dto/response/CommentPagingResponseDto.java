package security.ttaallkk.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import security.ttaallkk.common.Constant;
import security.ttaallkk.domain.post.Comment;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommentPagingResponseDto implements Serializable{

    private Long id; //댓글 아이디
    private String content; //댓글 내용
    private String uid; //댓글 작성자 uid
    private String email; //댓글 작성자 email
    private String displayName; //댓글 작성자 닉네임
    private String profileUrl; //댓글 작성자 프로필 이미지
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private Integer childrenCnt; //댓글 카운트

    // Dto 변환
    public static List<CommentPagingResponseDto> convertCommentPagingResponseDto(List<Comment> comments) {
        List<CommentPagingResponseDto> result = comments
            .stream()
            .map(comment -> new CommentPagingResponseDto(
                comment.getId(),
                comment.getIsDeleted() ? Constant.COMMENT_REMOVED_STATUS_MESSAGE : comment.getContent(),
                comment.getWriter().getUid(),
                comment.getWriter().getEmail(),
                comment.getWriter().getDisplayName(),
                comment.getWriter().getProfileUrl(),
                comment.getCreatedAt(),
                comment.getModifiedAt(),
                comment.getChildrenCnt()
                )
            ).collect(Collectors.toList());
        return result;
    }
}
