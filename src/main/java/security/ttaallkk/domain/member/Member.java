package security.ttaallkk.domain.member;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import security.ttaallkk.domain.CommonDateTime;
import security.ttaallkk.domain.post.Like;
import security.ttaallkk.domain.post.Post;
import security.ttaallkk.domain.post.DisLike;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.ko.KoreanFilterFactory;
import org.apache.lucene.analysis.ko.KoreanTokenizerFactory;
import org.apache.lucene.analysis.standard.ClassicTokenizerFactory;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @JsonIgnore : Entity를 json response로 보낼 시 해당 어노테이션이 적용된 컬럼은 제외.
 * @AnalyzerDef : Hibernate Search에 사용될 Analyzer 정의.(한글 전용은 '아리랑'이라 불리는 오픈소스)
 * @Analyzer : 정의된 Analyzer가 사용될 컬럼에 적용.
 */

 
@Entity
@Indexed
@AnalyzerDef(
    name = "koreanAnalyzer", 
    tokenizer = @TokenizerDef(factory = KoreanTokenizerFactory.class),
    filters = {@TokenFilterDef(factory = KoreanFilterFactory.class)})
@AnalyzerDef(
    name = "emailAnalyzer",
    tokenizer = @TokenizerDef(factory = ClassicTokenizerFactory.class),
    filters = {@TokenFilterDef(factory = LowerCaseFilterFactory.class)})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends CommonDateTime implements Serializable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    @JsonIgnore
    private Long id;

    @Column(name = "email", unique = true, nullable = false)
    @Field
    @Analyzer(definition = "emailAnalyzer")
    private String email;

    @Column(name = "password", nullable = false)
    @JsonIgnore
    private String password;

    @Column( name = "displayName", unique = true, nullable = false)
    @Field
    @Analyzer(definition = "koreanAnalyzer")
    private String displayName;

    @Column( name = "uid", unique = true, nullable = false)
    private String uid;

    @Column(name = "profileUrl")
    private String profileUrl;

    @Column(name = "roles")
    @ElementCollection(fetch = FetchType.LAZY)
    @Enumerated(EnumType.STRING)
    @JsonIgnore
    private Set<MemberRole> roles = new HashSet<>();

    @Column(name = "refreshToken")
    @JsonIgnore
    private String refreshToken;

    //게시글
    @JsonIgnore
    @OneToMany(mappedBy = "writer") //Post엔티티에서 Member필드명을 writer로 지정해놓았기 때문에 메핑을 해당 필드명으로 지정.
    private List<Post> posts = new ArrayList<>();

    //좋아요
    @JsonIgnore
    @OneToMany(mappedBy = "member")
    private Set<Like> likes = new HashSet<>();

    //싫어요
    @JsonIgnore
    @OneToMany(mappedBy = "member")
    private Set<DisLike> dislikes = new HashSet<>();

    @Builder
    public Member(String email, String password, String displayName, String uid, String profileUrl,Set<MemberRole> roles) {
        this.email = email;
        this.password = password;
        this.displayName = displayName;
        this.uid = uid;
        this.profileUrl = profileUrl;
        this.roles = roles;
    }

    //refreshToken 갱신
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    //프로필 정보 갱신
    public void updateProfile(String displayName, String profileUrl){
        this.displayName = displayName;
        this.profileUrl = profileUrl;
    }
}

