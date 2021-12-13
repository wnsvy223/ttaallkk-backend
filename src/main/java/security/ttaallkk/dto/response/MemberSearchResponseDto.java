package security.ttaallkk.dto.response;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import security.ttaallkk.domain.member.Member;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MemberSearchResponseDto implements Serializable{
    
    private Long id;

    private String email;

    private String uid;

    private String displayName;

    private String profileUrl;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    public static List<MemberSearchResponseDto> convertMemberSearchResponseDto(List<Member> members) {
        return members.stream().map(member -> new MemberSearchResponseDto(
                member.getId(),
                member.getEmail(),
                member.getUid(),
                member.getDisplayName(),
                member.getProfileUrl(),
                member.getCreatedAt(),
                member.getModifiedAt()
            )
        ).collect(Collectors.toList());
    }
}
