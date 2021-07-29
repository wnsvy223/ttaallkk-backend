package security.ttaallkk.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import security.ttaallkk.domain.post.Comment;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentResponseDto implements Serializable{
    
    private Long id; //댓글 아이디
    private String content; //댓글 내용
    private String uid; //댓글 작성자 uid
    private String email; //댓글 작성자 email
    private String displayName; //댓글 작성자 닉네임
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
    private List<CommentResponseDto> children = new ArrayList<>(); //대댓글목록

    public CommentResponseDto(
                Long id, 
                String content, 
                String uid, 
                String email, 
                String displayName, 
                LocalDateTime createdAt, 
                LocalDateTime modifiedAt) {
        this.id = id;
        this.content = content;
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
    }

    public static CommentResponseDto convertCommentToDto(Comment comment) {
        return comment.getIsDeleted()  == true ? 
                new CommentResponseDto(comment.getId(), "삭제된 댓글입니다.", null, null, null, comment.getCreatedAt(), comment.getModifiedAt()) :
                new CommentResponseDto(comment.getId(), comment.getContent(), comment.getWriter().getUid(), comment.getWriter().getEmail(), comment.getWriter().getDisplayName(), comment.getCreatedAt(), comment.getModifiedAt());
    }
}
