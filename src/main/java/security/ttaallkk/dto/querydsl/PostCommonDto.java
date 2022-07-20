package security.ttaallkk.dto.querydsl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;
import security.ttaallkk.common.Constant;
import security.ttaallkk.domain.post.Post;
import security.ttaallkk.domain.post.PostStatus;


@Getter
public class PostCommonDto{

    private String uid;
    
    private String email;

    private String displayName;

    private String profileUrl;

    private Long id;
    
    private String title;

    private Integer commentCnt;

    private Integer likeCnt;

    private Integer disLikeCnt;

    private Integer views;

    private PostStatus postStatus;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    private String categoryName;

    private String categoryTag;

    @QueryProjection
    public PostCommonDto(
                String uid,
                String email, 
                String displayName, 
                String profileUrl, 
                Long id,
                String title, 
                Integer commentCnt,
                Integer likeCnt,
                Integer disLikeCnt,
                Integer views,
                PostStatus postStatus,
                LocalDateTime createdAt,
                LocalDateTime modifiedAt,
                String categoryName,
                String categoryTag) {

        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.profileUrl = profileUrl;
        this.id = id;
        this.title = title;
        this.commentCnt = commentCnt;
        this.likeCnt = likeCnt;
        this.disLikeCnt = disLikeCnt;
        this.views = views;
        this.postStatus = postStatus;
        this.createdAt = createdAt;
        this.modifiedAt = modifiedAt;
        this.categoryName = categoryName;
        this.categoryTag = categoryTag;
    }

    /**
     * 커스텀 Dto로 변환
     * @param List<Post>
     * @return List<PostWithMemberDto>
     */
    public static List<PostCommonDto> convertPostCommonDto(List<Post> posts) {
        List<PostCommonDto> result = new ArrayList<>();
        posts.stream().forEach(post -> {
            PostCommonDto postCommonDto = new PostCommonDto(
                post.getWriter().getUid(),
                post.getWriter().getEmail(), 
                post.getWriter().getDisplayName(), 
                post.getWriter().getProfileUrl(), 
                post.getId(), 
                post.getPostStatus() == PostStatus.REMOVED ? Constant.POST_REMOVED_STATUS_MESSAGE : post.getTitle(), 
                post.getCommentCnt(), 
                post.getLikeCnt(),
                post.getDislikeCnt(),
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

    /**
     * 커스텀 Dto로 반환(QueryDsl에서 Dto로 반환받은 값 중 필요한 부분 수정 후 반환)
     * @param posts
     * @return List<PostCommonDto>
     */
    public static List<PostCommonDto> convertPostCommonDtoElement(List<PostCommonDto> posts) {
        List<PostCommonDto> result = new ArrayList<>();
        posts.stream().forEach(post -> {
            PostCommonDto postCommonDto = new PostCommonDto(
                post.getUid(),
                post.getEmail(), 
                post.getDisplayName(), 
                post.getProfileUrl(), 
                post.getId(), 
                post.getPostStatus() == PostStatus.REMOVED ? Constant.POST_REMOVED_STATUS_MESSAGE : post.getTitle(), 
                post.getCommentCnt(), 
                post.getLikeCnt(),
                post.getDisLikeCnt(),
                post.getViews(), 
                post.getPostStatus(), 
                post.getCreatedAt(), 
                post.getModifiedAt(),
                post.getCategoryName(),
                post.getCategoryTag());
            result.add(postCommonDto);
        });
        return result;
    }
}
