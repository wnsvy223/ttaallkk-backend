package security.ttaallkk.dto.response;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import security.ttaallkk.common.Constant;
import security.ttaallkk.domain.post.Category;
import security.ttaallkk.domain.post.Post;
import security.ttaallkk.domain.post.PostStatus;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDetailResponseDto {
    
    //Post;
    private Long id;
    
    private String title;

    private String content;
    
    private Integer commentCnt;

    private Integer likeCnt;

    private Integer disLikeCnt;

    private Integer views;

    private PostStatus postStatus;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    private Category category;

    //Member
    private String email;

    private String displayName;

    private String uid;

    private String profileUrl;

    //Comments
    @Builder.Default
    private List<CommentResponseDto> comments = new ArrayList<>();

    //Like
    private Boolean isAlreadyLike;

    //UnLike
    private Boolean isAlreadyDisLike;

    /**
     * 조회된 Post 엔티티 데이터로 사용자의 좋아요 유무 및 조회수 증가로직 수행 후 PostDetailResponseDto 반환
     * @param Post
     * @return PostDetailResponseDto
     */
    public static PostDetailResponseDto convertPostDetailResponseDto(Post post, Boolean isLike, Boolean isDisLike) {
        PostDetailResponseDto postDetailResponseDto = PostDetailResponseDto.builder() //PostDetailResponseDto생성하여 데이터 세팅 후 반환
            .id(post.getId())
            .title(post.getPostStatus() == PostStatus.REMOVED ? Constant.POST_REMOVED_STATUS_MESSAGE : post.getTitle())
            .content(post.getPostStatus() == PostStatus.REMOVED ? Constant.POST_REMOVED_STATUS_MESSAGE : post.getContent())
            .commentCnt(post.getCommentCnt())
            .likeCnt(post.getLikeCnt())
            .disLikeCnt(post.getDislikeCnt())
            .views(post.getViews())
            .postStatus(post.getPostStatus())
            .createdAt(post.getCreatedAt())
            .modifiedAt(post.getModifiedAt())
            .category(post.getCategory())
            .email(post.getWriter().getEmail())
            .displayName(post.getWriter().getDisplayName())
            .uid(post.getWriter().getUid())
            .profileUrl(post.getWriter().getProfileUrl())
            .isAlreadyLike(isLike)
            .isAlreadyDisLike(isDisLike)
            .build();
        
        return postDetailResponseDto;
    }
}
