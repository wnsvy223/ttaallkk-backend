package security.ttaallkk.dto.querydsl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import security.ttaallkk.domain.post.Post;
import security.ttaallkk.domain.post.PostStatus;


@Getter
public class PostCommonDto{
    
    private String email;

    private String displayName;

    private String profileUrl;

    private Long id;
    
    private String title;

    private Integer commentCnt;

    private Integer likeCnt;

    private Integer views;

    private PostStatus postStatus;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    private String categoryName;

    @QueryProjection
    public PostCommonDto(
                String email, 
                String displayName, 
                String profileUrl, 
                Long id,
                String title, 
                Integer commentCnt,
                Integer likeCnt,
                Integer views,
                PostStatus postStatus,
                LocalDateTime createdAt,
                LocalDateTime modifiedAt,
                String categoryName) {

        this.email = email;
        this.displayName = displayName;
        this.profileUrl = profileUrl;
        this.id = id;
        this.title = title;
        this.commentCnt = commentCnt;
        this.likeCnt = likeCnt;
        this.views = views;
        this.postStatus = postStatus;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.categoryName = categoryName;
    }

    /**
     * 게시글 Full Text Search 결과값에 댓글 카운트 추가를 위해 댓글 카운트가 포함된 커스텀 Dto로 변환
     * @param List<Post>
     * @return List<PostWithMemberDto>
     */
    public static List<PostCommonDto> convertPostWithMemberDto(List<Post> posts) {
        List<PostCommonDto> result = new ArrayList<>();
        posts.stream().forEach(post -> {
            PostCommonDto postWithMemberDto = new PostCommonDto(
                post.getWriter().getEmail(), 
                post.getWriter().getDisplayName(), 
                post.getWriter().getProfileUrl(), 
                post.getId(), 
                post.getTitle(), 
                post.getCommentCnt(), 
                post.getLikeCnt(), 
                post.getViews(), 
                post.getPostStatus(), 
                post.getCreatedAt(), 
                post.getModifiedAt(),
                post.getCategory().getCtgName());
            result.add(postWithMemberDto);
        });
        return result;
    }
}
