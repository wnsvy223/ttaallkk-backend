package security.ttaallkk.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DisLikeCreateDto {
    
    Long postId;

    String uid;

}
