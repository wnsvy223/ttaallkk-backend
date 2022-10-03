package security.ttaallkk.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import security.ttaallkk.common.Constant;
import security.ttaallkk.domain.post.Comment;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentChildPagingResponseDto implements Serializable{
    
    private Long id; //댓글 아이디

    private Long parent; //부모 댓글 아이디

    private Long toCommentId; //타겟 댓글 아이디

    private Boolean isDeleted; //댓글 삭제 상태 유무

    private String content; //댓글 내용

    private String uid; //댓글 작성자 uid

    private String email; //댓글 작성자 email

    private String displayName; //댓글 작성자 닉네임

    private String profileUrl; //댓글 작성자 프로필 이미지

    private String toUid; //댓글 타겟 유저 uid

    private String toEmail; //댓글 타겟 유저 email

    private String toDisplayName; //댓글 타겟 유저 닉네임 

    private String toProfileUrl; //댓글 타겟 유저 프로필 이미지

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    private String postWriterUid; //게시글 작성자 uid

    private String postWriterEmail; //게시글 작성자 email

    private String postWriterDisplayName; //게시글 작성자 닉네임

    private String postWriterProfileUrl; //게시글 작성자 프로필 이미지


     // Dto 변환
     public static List<CommentChildPagingResponseDto> convertCommentPagingResponseDto(List<Comment> comments) {
        List<CommentChildPagingResponseDto> result = comments
            .stream()
            .map(comment -> new CommentChildPagingResponseDto(
                comment.getId(),
                comment.getParent() == null ? null : comment.getParent().getId(),
                comment.getToComment() == null ? null : comment.getToComment().getId(),
                comment.getIsDeleted(),
                comment.getIsDeleted() ? Constant.COMMENT_REMOVED_STATUS_MESSAGE : comment.getContent(),
                comment.getWriter().getUid(),
                comment.getWriter().getEmail(),
                comment.getWriter().getDisplayName(),
                comment.getWriter().getProfileUrl(),
                comment.getToComment() == null ? null : comment.getToComment().getWriter().getUid(),
                comment.getToComment() == null ? null : comment.getToComment().getWriter().getEmail(),
                comment.getToComment() == null ? null : comment.getToComment().getWriter().getDisplayName(),
                comment.getToComment() == null ? null : comment.getToComment().getWriter().getProfileUrl(),
                comment.getCreatedAt(),
                comment.getModifiedAt(),
                comment.getPost().getWriter().getUid(),
                comment.getPost().getWriter().getEmail(),
                comment.getPost().getWriter().getDisplayName(),
                comment.getPost().getWriter().getProfileUrl()
                )
            ).collect(Collectors.toList());
        return result;
    }
}
