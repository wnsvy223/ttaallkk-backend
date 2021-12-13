package security.ttaallkk.dto.request;

import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class FriendUpdateDto {
    
    @NotNull
    private String fromUserUid;
}
