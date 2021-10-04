package security.ttaallkk.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Builder
public class LoginResponse {
    private int status;

    private String message;

    private String email;

    private String uid;

    private String displayName;

    private String profileUrl;

    @JsonIgnore
    private String accessToken;

    private LocalDateTime expiredAt; //만료 시간

    @JsonIgnore
    private String refreshToken;

    private LocalDateTime issuedAt; //발급 시간
}
