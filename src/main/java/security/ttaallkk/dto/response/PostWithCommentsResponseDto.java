package security.ttaallkk.dto.response;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import security.ttaallkk.domain.post.Post;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostWithCommentsResponseDto {
    
    private Post post;

    @Builder.Default
    private List<CommentResponseDto> comments = new ArrayList<>();
}
