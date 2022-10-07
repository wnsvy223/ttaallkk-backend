package security.ttaallkk.dto.common;

import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileCommonDto {
    
    String fileName;

    String fileBase64String;

    String dataUrl;

}
