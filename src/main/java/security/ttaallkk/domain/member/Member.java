package security.ttaallkk.domain.member;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import security.ttaallkk.domain.CommonDateTime;
import security.ttaallkk.domain.post.Like;
import security.ttaallkk.domain.post.Post;
import security.ttaallkk.domain.post.DisLike;

import javax.persistence.CascadeType;
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
import org.apache.lucene.analysis.ngram.EdgeNGramFilterFactory;
import org.apache.lucene.analysis.ngram.NGramFilterFactory;
import org.apache.lucene.analysis.ngram.NGramTokenizerFactory;
import org.hibernate.search.annotations.Analyzer;
import org.hibernate.search.annotations.AnalyzerDef;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;
import org.hibernate.search.annotations.TokenFilterDef;
import org.hibernate.search.annotations.TokenizerDef;
import org.hibernate.search.annotations.Parameter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @JsonIgnore : Entity를 json response로 보낼 시 해당 어노테이션이 적용된 컬럼은 제외.
 * @AnalyzerDef : Hibernate Search에 사용될 Analyzer 정의.(한글 전용은 '아리랑'이라 불리는 오픈소스)
 * @Analyzer : 정의된 Analyzer가 사용될 컬럼에 적용.
 * 필터 가이드 문서 : https://solr.apache.org/guide/6_6/filter-descriptions.html
 */

 
@Entity
@Indexed
@AnalyzerDef(
    name = "koreanAnalyzer", 
    tokenizer = @TokenizerDef(factory = KoreanTokenizerFactory.class), //한글 Arirang Tokenizer
    filters = {
        @TokenFilterDef(factory = KoreanFilterFactory.class), //한글 Arirang Token Filter : 한글 문법에 맞게 토큰화
        @TokenFilterDef(factory = NGramFilterFactory.class, //NGram Filter : 1~10 단위 글자로 토큰화
            params = {
                @Parameter(name = "minGramSize", value = "1"),
                @Parameter(name = "maxGramSize", value = "10")
            }
        ),
        @TokenFilterDef(factory = EdgeNGramFilterFactory.class, //EdgeNGram Filter : 1~10 단위 글자로 후위순서 토큰화
            params = {
                @Parameter(name = "minGramSize", value = "1"),
                @Parameter(name = "maxGramSize", value = "10")
            }
        )
    }
)
@AnalyzerDef(
    name = "emailAnalyzer",
    tokenizer = @TokenizerDef(factory = NGramTokenizerFactory.class, //NGram Tokenizer : 1~10단위 글자로 토큰화
        params = {
            @Parameter(name = "minGramSize", value = "1"),
            @Parameter(name = "maxGramSize", value = "10")
        }),
    filters = {
        @TokenFilterDef(factory = LowerCaseFilterFactory.class) // 소문자 변환 필터
    }
)
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

    @Column(name = "deviceToken")
    private String deviceToken;

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

    //내가 친구추가 요청한 친구 목록
    @JsonIgnore
    @OneToMany(mappedBy = "from", cascade = CascadeType.REMOVE, orphanRemoval = true)
    List<Friend> requestFriend;

    //나에게 친구추가 요청한 친구 목록
    @JsonIgnore
    @OneToMany(mappedBy = "to", cascade = CascadeType.REMOVE, orphanRemoval = true)
    List<Friend> receiveFriend;

    @Builder
    public Member(String email, String password, String displayName, String uid, String profileUrl,Set<MemberRole> roles) {
        this.email = email;
        this.password = password;
        this.displayName = displayName;
        this.uid = uid;
        this.profileUrl = profileUrl;
        this.roles = roles;
    }

    //refreshToken 업데이트
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    //프로필 정보 업데이트
    public void updateProfile(String displayName) {
        this.displayName = displayName;
    }

    //프로필 이미지 업데이트
    public void updateProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    //디바이스 토큰 업데이트
    public void updateDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }
}

