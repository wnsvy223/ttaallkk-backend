package security.ttaallkk.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CategoryCreateDto {
    
    private String categoryTag;

    private String categoryName;

    private String description;
}
