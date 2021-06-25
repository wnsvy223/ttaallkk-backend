package security.ttaallkk.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenDto {
    private String grantType;
    //private String refreshToken;
    private String email;
}
