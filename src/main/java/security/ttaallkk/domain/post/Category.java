package security.ttaallkk.domain.post;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Formula;
import org.hibernate.search.annotations.Field;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category {
    
    @Id
    @Field(name = "category_id") //hibernate search orm에서 검색조건에 사용하기 위해 인덱스 필드로 지정(id 이름은 예약어 이므로 category_id로 지정)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    private String ctgName;

    @Formula("(select count(*) from post p where p.category = category_id)")
    private Integer postCount;

    private String description;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Post> posts = new ArrayList<>();

    @Builder
    public Category(String ctgName, String description){
        this.ctgName = ctgName;
        this.description = description;
    }
}
