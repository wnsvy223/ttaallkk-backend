package security.ttaallkk.domain;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member {
    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;

    @Column(unique = true)
    private String email;
    private String password;
    private String displayName;
    private String uid;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<MemberRole> roles = new HashSet<>();

    private String refreshToken;

    @Builder
    public Member(String email, String password, String displayName, String uid, Set<MemberRole> roles) {
        this.email = email;
        this.password = password;
        this.displayName = displayName;
        this.uid = uid;
        this.roles = roles;
    }

    //refreshToken 갱신
    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}

