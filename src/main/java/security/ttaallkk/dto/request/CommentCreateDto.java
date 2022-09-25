package security.ttaallkk.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentCreateDto {

    private String content; // 댓글 본문

    private Long postId; // 게시글 아이디

    private Long parentId;  // 부모댓글 아이디

    private Long toCommentId; // 타겟 댓글 아이디
   
    private String writerUid; // 댓글 작성자 uid
}
