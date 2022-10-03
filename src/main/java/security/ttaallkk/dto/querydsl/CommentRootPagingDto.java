package security.ttaallkk.dto.querydsl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import security.ttaallkk.common.Constant;

@Getter
public class CommentRootPagingDto {
    
    private Long id; //댓글 아이디

    private Long parent; //부모 댓글 아이디

    private Boolean isDeleted; //댓글 삭제 상태 유무

    private String content; //댓글 내용

    private String uid; //댓글 작성자 uid

    private String email; //댓글 작성자 email

    private String displayName; //댓글 작성자 닉네임

    private String profileUrl; //댓글 작성자 프로필 이미지

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    private Long childrenCnt; //댓글 카운트

    private Boolean isContainOwnerComment; //대댓글에 게시글 작성자의 대댓글이 존재하는지 유무

    private String postWriterUid; //게시글 작성자 uid

    private String postWriterEmail; //게시글 작성자 email

    private String postWriterDisplayName; //게시글 작성자 닉네임

    private String postWriterProfileUrl; //게시글 작성자 프로필 이미지


    @QueryProjection
    public CommentRootPagingDto(
        Long id,
        Long parent,
        Boolean isDeleted,
        String content,
        String uid,
        String email,
        String displayName,
        String profileUrl,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        Long childrenCnt,
        Boolean isContainOwnerComment,
        String postWriterUid,
        String postWriterEmail,
        String postWriterDisplayName,
        String postWriterProfileUrl
    ){
        this.id = id;
        this.parent = parent;
        this.isDeleted = isDeleted;
        this.content = content;
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.profileUrl = profileUrl;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.childrenCnt = childrenCnt;
        this.isContainOwnerComment = isContainOwnerComment;
        this.postWriterUid = postWriterUid;
        this.postWriterEmail = postWriterEmail;
        this.postWriterDisplayName = postWriterDisplayName;
        this.postWriterProfileUrl = postWriterProfileUrl;
    }

    // 댓글 삭제상태에 따라 댓글 내용값 변경된 Dto로 변환
    public static List<CommentRootPagingDto> convertCommentRootPagingDto(List<CommentRootPagingDto> comments) {
        return comments
            .stream()
            .map(comment -> new CommentRootPagingDto(
                    comment.getId(),
                    comment.getParent(),
                    comment.getIsDeleted(),
                    comment.getIsDeleted() ? Constant.COMMENT_REMOVED_STATUS_MESSAGE : comment.getContent(),
                    comment.getUid(),
                    comment.getEmail(),
                    comment.getDisplayName(),
                    comment.getProfileUrl(),
                    comment.getCreatedAt(),
                    comment.getModifiedAt(),
                    comment.getChildrenCnt(),
                    comment.getIsContainOwnerComment(),
                    comment.getPostWriterUid(),
                    comment.getPostWriterEmail(),
                    comment.getPostWriterDisplayName(),
                    comment.getPostWriterProfileUrl()
                )
            ).collect(Collectors.toList());
    }
}
