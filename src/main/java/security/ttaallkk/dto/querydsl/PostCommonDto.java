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

    private Integer unLikeCnt;

    private Integer views;

    private PostStatus postStatus;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    private String categoryName;

    private String categoryTag;

    @QueryProjection
    public PostCommonDto(
                String email, 
                String displayName, 
                String profileUrl, 
                Long id,
                String title, 
                Integer commentCnt,
                Integer likeCnt,
                Integer unLikeCnt,
                Integer views,
                PostStatus postStatus,
                LocalDateTime createdAt,
                LocalDateTime modifiedAt,
                String categoryName,
                String categoryTag) {

        this.email = email;
        this.displayName = displayName;
        this.profileUrl = profileUrl;
        this.id = id;
        this.title = title;
        this.commentCnt = commentCnt;
        this.likeCnt = likeCnt;
        this.unLikeCnt = unLikeCnt;
        this.views = views;
        this.postStatus = postStatus;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.categoryName = categoryName;
        this.categoryTag = categoryTag;
    }

    /**
     * 게시글 Full Text Search 결과값에 댓글 카운트 추가를 위해 댓글 카운트가 포함된 커스텀 Dto로 변환
     * @param List<Post>
     * @return List<PostWithMemberDto>
     */
    public static List<PostCommonDto> convertPostCommonDto(List<Post> posts) {
        List<PostCommonDto> result = new ArrayList<>();
        posts.stream().forEach(post -> {
            PostCommonDto postCommonDto = new PostCommonDto(
                post.getWriter().getEmail(), 
                post.getWriter().getDisplayName(), 
                post.getWriter().getProfileUrl(), 
                post.getId(), 
                post.getTitle(), 
                post.getCommentCnt(), 
                post.getLikeCnt(),
                post.getUnlikeCnt(),
                post.getViews(), 
                post.getPostStatus(), 
                post.getCreatedAt(), 
                post.getModifiedAt(),
                post.getCategory().getCtgName(),
                post.getCategory().getCtgTag());
            result.add(postCommonDto);
        });
        return result;
    }
}
