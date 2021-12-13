package security.ttaallkk.dto.querydsl;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import security.ttaallkk.domain.member.Friend;
import security.ttaallkk.domain.member.FriendStatus;
import security.ttaallkk.dto.response.MemberResponsDto;

import java.io.Serializable;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class FriendResponseDto implements Serializable {

    Long friendId;

    MemberResponsDto fromUser;

    MemberResponsDto toUser;

    FriendStatus friendStatus;

    public static FriendResponseDto convertFriendToDto(Friend friend) {
        return new FriendResponseDto(
            friend.getId(),
            MemberResponsDto.convertUserToDto(friend.getFrom()),
            MemberResponsDto.convertUserToDto(friend.getTo()),
            friend.getFriendStatus()
        );
    }
}
