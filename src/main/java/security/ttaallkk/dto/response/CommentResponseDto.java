package security.ttaallkk.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import security.ttaallkk.common.Constant;
import security.ttaallkk.domain.post.Comment;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDto implements Serializable{
    
    private Long id; //댓글 아이디
    private Long parent; //부모 댓글 아이디
    private Long toCommentId; //타겟 댓글 아이디
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
    private List<CommentResponseDto> children = new ArrayList<>(); //대댓글목록(자식댓글 필드는 생성자에서 받지않고 convertCommentStructure메소드에서 계층형 구조로 변환 후 추가하여 반환)

    public CommentResponseDto(
                Long id, 
                Long parent,
                Long toCommentId,
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
                LocalDateTime modifiedAt) {
        this.id = id;
        this.parent = parent;
        this.toCommentId = toCommentId;
        this.content = content;
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
    }

    // Comment Entity -> CommentResponseDto 변환
    public static CommentResponseDto convertCommentToDto(Comment comment) {
        return new CommentResponseDto(
            comment.getId(),
            comment.getParent() == null ? null : comment.getParent().getId() ,
            comment.getToComment() == null ? null : comment.getToComment().getId(),
            comment.getIsDeleted() == true ? Constant.COMMENT_REMOVED_STATUS_MESSAGE : comment.getContent() ,
            comment.getWriter().getUid(),
            comment.getWriter().getEmail(),
            comment.getWriter().getDisplayName(),
            comment.getWriter().getProfileUrl(),
            comment.getToComment() == null ? null : comment.getToComment().getWriter().getUid(),
            comment.getToComment() == null ? null : comment.getToComment().getWriter().getEmail(),
            comment.getToComment() == null ? null : comment.getToComment().getWriter().getDisplayName(),
            comment.getToComment() == null ? null : comment.getToComment().getWriter().getProfileUrl(),
            comment.getCreatedAt(),
            comment.getModifiedAt());
    }

    /**
     * DB에서 조회된 댓글 데이터를 계층형 댓글구조로 변환하여 반환
     * @param comments
     * @return List<CommentResponseDto>
     */
    public static List<CommentResponseDto> convertCommentStructure(List<Comment> comments) {
        List<CommentResponseDto> result = new ArrayList<>();
        Map<Long, CommentResponseDto> map = new HashMap<>();
        comments.stream().forEach(c -> {
            CommentResponseDto commentResponseDto = CommentResponseDto.convertCommentToDto(c);
            map.put(commentResponseDto.getId(), commentResponseDto);
            if(c.getParent() != null && map.containsKey(c.getParent().getId())){
                map.get(c.getParent().getId()).getChildren().add(commentResponseDto);
            }else{
                result.add(commentResponseDto);
            }
        });
        return result;
    }
}
