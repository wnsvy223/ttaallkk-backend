package security.ttaallkk.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import security.ttaallkk.common.Constant;
import security.ttaallkk.domain.post.Comment;

@Getter
public class CommentPagingResponseDto implements Serializable{

    private Long id; //댓글 아이디

    private Long parent; //부모 댓글 아이디

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

    private Integer childrenCnt; //댓글 카운트

    // Dto 변환
    public static List<CommentPagingResponseDto> convertCommentPagingResponseDto(List<Comment> comments) {
        List<CommentPagingResponseDto> result = comments
            .stream()
            .map(comment -> new CommentPagingResponseDto(
                comment.getId(),
                comment.getParent() != null ?  comment.getParent().getId() : null,
                comment.getIsDeleted(),
                comment.getIsDeleted() ? Constant.COMMENT_REMOVED_STATUS_MESSAGE : comment.getContent(),
                comment.getWriter().getUid(),
                comment.getWriter().getEmail(),
                comment.getWriter().getDisplayName(),
                comment.getWriter().getProfileUrl(),
                comment.getToUser() == null ? null : comment.getToUser().getUid(),
                comment.getToUser() == null ? null : comment.getToUser().getEmail(),
                comment.getToUser() == null ? null : comment.getToUser().getDisplayName(),
                comment.getToUser() == null ? null : comment.getToUser().getProfileUrl(),
                comment.getCreatedAt(),
                comment.getModifiedAt(),
                comment.getChildrenCnt()
                )
            ).collect(Collectors.toList());
        return result;
    }

    @QueryProjection
    public CommentPagingResponseDto(
                Long id,
                Long parent,
                Boolean isDeleted,
                String content,
                String uid,
                String email,
                String displayName,
                String profileUrl,
                String toUid,
                String toEmail,
                String toDisplayName,
                String toProfileUrl,
                LocalDateTime createdAt,
                LocalDateTime modifiedAt,
                Integer childrenCnt) {

        this.id = id;
        this.parent = parent;
        this.isDeleted = isDeleted;
        this.content = (isDeleted == true) ? Constant.COMMENT_REMOVED_STATUS_MESSAGE : content;
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.profileUrl = profileUrl;
        this.toUid = toUid;
        this.toEmail = toEmail;
        this.toDisplayName = toDisplayName;
        this.toProfileUrl = toProfileUrl;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.childrenCnt = childrenCnt;
    }
}
