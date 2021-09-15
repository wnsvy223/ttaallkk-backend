package security.ttaallkk.dto.querydsl;

import com.querydsl.core.annotations.QueryProjection;

import lombok.Getter;

@Getter
public class LikeCommonDto {
    
    Long id;

    Long postId;

    Integer likeCnt;

    String title;

    String uid;

    String email;

    String displayName;

    String profileUrl;

    @QueryProjection
    public LikeCommonDto(
                Long id,
                Long postId,
                Integer likeCnt,
                String title,
                String uid,
                String email,
                String displayName,
                String profileUrl) {

        this.id = id;
        this.postId = postId;
        this.likeCnt = likeCnt;
        this.title = title;
        this.uid = uid;
        this.email = email;
        this.displayName = displayName;
        this.profileUrl = profileUrl;
    }
}
