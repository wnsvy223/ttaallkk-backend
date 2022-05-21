package security.ttaallkk.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import security.ttaallkk.domain.member.Member;

import java.io.Serializable;
import java.time.LocalDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class MemberResponsDto implements Serializable {
    
    private Long id;

    private String email;

    private String uid;

    private String displayName;

    private String profileUrl;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    public static MemberResponsDto convertUserToDto(Member member) {
        return new MemberResponsDto(
            member.getId(),
            member.getEmail(),
            member.getUid(),
            member.getDisplayName(),
            member.getProfileUrl(),
            member.getCreatedAt(),
            member.getModifiedAt()
        );
    }
}
