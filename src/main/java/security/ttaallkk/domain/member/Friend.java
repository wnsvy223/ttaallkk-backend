package security.ttaallkk.domain.member;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Friend{
    
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "friend_id")
    private Long id;

    //친구 관계 주체 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_id")
    private Member from;

    //친구 관계 상대 유저
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_id")
    private Member to;

    //수락 상태 (초기값은 IDLE)
    @Enumerated(EnumType.STRING)
    private FriendStatus friendStatus = FriendStatus.IDLE;

    //친구 관계 데이터 생성
    public static Friend createFriend(Member from, Member to) {
        Friend friend = new Friend();
        friend.from = from;
        friend.to = to;
        friend.friendStatus = FriendStatus.IDLE;
        return friend;
    }

    //친구 추가 승인
    public void updateToUserAccept() {
        this.friendStatus = FriendStatus.ACCEPT;
    }

    //친구 추가 거절
    public void updateToUserReject() {
        this.friendStatus = FriendStatus.REJECT;
    }
}
