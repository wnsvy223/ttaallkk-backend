package security.ttaallkk.service.member;

import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import security.ttaallkk.domain.member.Friend;
import security.ttaallkk.domain.member.Member;
import security.ttaallkk.dto.response.FriendResponseDto;
import security.ttaallkk.dto.response.MemberResponsDto;
import security.ttaallkk.exception.FriendAlreadyExistException;
import security.ttaallkk.exception.FriendNotAllowSelfException;
import security.ttaallkk.exception.FriendRelationNotFoundException;
import security.ttaallkk.exception.MemberNotFoundException;
import security.ttaallkk.repository.member.FriendRepository;
import security.ttaallkk.repository.member.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FriendService {
    
    private final FriendRepository friendRepository;
    private final MemberRepository memberRepository;

    /**
     * 친구 요청
     * @param toUserUid
     * @return MemberResponsDto
     */
    @Transactional
    public MemberResponsDto requestFriend(String toUserUid) {
        //친구 요청한 유저
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member fromUser = memberRepository.findMemberByEmail(authentication.getName()).orElseThrow(MemberNotFoundException::new);
        //친구 요청받은 유저 
        Member toUser = memberRepository.findMemberByUid(toUserUid).orElseThrow(MemberNotFoundException::new);
        if(fromUser.getId() == toUser.getId()) {
            throw new FriendNotAllowSelfException(); //자기 자신 친구추가 불가 예외
        }
        friendRepository.findByFromAndTo(fromUser, toUser).ifPresent(friend -> {
            throw new FriendAlreadyExistException(); //이미 친구 상태인 경우 예외
        });
        //친구 관계 데이터 생성
        Friend friend = friendRepository.save(Friend.createFriend(fromUser, toUser)); 
        
        return MemberResponsDto.convertUserToDto(friend.getTo()); //친구추가 타겟유저 정보 반환
    }

    /**
     * 친구 수락
     * @param fromUserUid
     * @return MemberResponsDto
     */
    @Transactional
    public MemberResponsDto acceptFriend(String fromUserUid) {
        //친구 요청받은 유저
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member toUser = memberRepository.findMemberByEmail(authentication.getName()).orElseThrow(MemberNotFoundException::new);
        //친구 요청한 유저
        Member fromUser = memberRepository.findMemberByUid(fromUserUid).orElseThrow(MemberNotFoundException::new);
        if(fromUser.getId() == toUser.getId()) {
            throw new FriendNotAllowSelfException(); //자기 자신 친구추가 불가 예외
        }
        Friend friend = friendRepository.findByToAndFrom(toUser, fromUser).orElseThrow(FriendRelationNotFoundException::new);
        friend.updateToUserAccept(); //친구 추가 요청 수락
        
        return MemberResponsDto.convertUserToDto(fromUser);
    }

    /**
     * 친구 거절
     * @param fromUserUid
     * @return MemberResponsDto
     */
    @Transactional
    public MemberResponsDto rejectFriend(String fromUserUid) {
        //친구 요청받은 유저
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member toUser = memberRepository.findMemberByEmail(authentication.getName()).orElseThrow(MemberNotFoundException::new);
        //친구 요청한 유저
        Member fromUser = memberRepository.findMemberByUid(fromUserUid).orElseThrow(MemberNotFoundException::new);
        if(fromUser.getId() == toUser.getId()) {
            throw new FriendNotAllowSelfException(); //자기 자신 친구추가 불가 예외
        }
        Friend friend = friendRepository.findByToAndFrom(toUser, fromUser).orElseThrow(FriendRelationNotFoundException::new);
        friend.updateToUserReject(); //친구 추가 요청 거절
        
        return MemberResponsDto.convertUserToDto(fromUser);
    }

    /**
     * 친구 목록 조회 + 페이징
     * @param id
     * @param pageable
     * @return Slice<FriendResponseDto>
     */
    @Transactional
    public Slice<FriendResponseDto> findFriendsByCurrentUser(String uid, Pageable pageable) {
        Member member = memberRepository.findMemberByUid(uid).orElseThrow(MemberNotFoundException::new);
        Slice<FriendResponseDto> friends = friendRepository.findMyFriendsByUserIdOrderByUid(member.getId(), pageable).map(f -> FriendResponseDto.convertFriendToDto(f));
        
        return friends;
    }

    /**
     * uid로 친구 목록 조회
     * @param uid
     * @return List<Friend>
     */
    @Transactional
    public List<Friend> findFromOrToByUid(String uid) {
        return friendRepository.findFromOrToByUid(uid);
    }
}
