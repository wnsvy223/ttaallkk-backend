package security.ttaallkk.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MemberUpdateResponseDto {

    private String message;

    private int status;

    private String uid;
    
    private String email;

    private String displayName;

    private String profileUrl;

}
