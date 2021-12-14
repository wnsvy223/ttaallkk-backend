package security.ttaallkk.controller.member;

import javax.validation.Valid;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import security.ttaallkk.dto.request.FriendCreateDto;
import security.ttaallkk.dto.request.FriendUpdateDto;
import security.ttaallkk.dto.response.FriendResponseDto;
import security.ttaallkk.dto.response.MemberResponsDto;
import security.ttaallkk.service.member.FriendService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;


@RestController
@RequestMapping("/api/friend")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    
    /**
     * 친구 관계 생성
     * @param friendCreateDto
     * @return MemberResponsDto 친구 추가한 사용자 정보
     */
    @PostMapping
    public ResponseEntity<MemberResponsDto> requestFriend(@Valid @RequestBody FriendCreateDto friendCreateDto) {
        MemberResponsDto memberResponsDto = friendService.requestFriend(friendCreateDto.getToUserUid());
        
        return ResponseEntity.ok(memberResponsDto);
    }

    /**
     * 친구 추가 승인
     * @param friendUpdateDto
     * @return MemberResponsDto 친구 추가한 사용자 정보
     */
    @PutMapping("/accept")
    public ResponseEntity<MemberResponsDto> acceptFriend(@Valid @RequestBody FriendUpdateDto friendUpdateDto) {
        MemberResponsDto memberResponsDto = friendService.acceptFriend(friendUpdateDto.getFromUserUid());
        
        return ResponseEntity.ok(memberResponsDto);
    }

    /**
     * 친구 추가 거절
     * @param friendUpdateDto
     * @return MemberResponsDto 친구 추가한 사용자 정보
     */
    @PutMapping("/reject")
    public ResponseEntity<MemberResponsDto> rejectFriend(@Valid @RequestBody FriendUpdateDto friendUpdateDto) {
        MemberResponsDto memberResponsDto = friendService.rejectFriend(friendUpdateDto.getFromUserUid());
        
        return ResponseEntity.ok(memberResponsDto);
    }

    /**
     * 현재 사용자의 친구 목록 + 친구 관계 상태
     * @param page
     * @param pageable
     * @param id
     * @return Slice<FriendResponseDto>
     */
    @GetMapping("/{id}")
    public ResponseEntity<Slice<FriendResponseDto>> getCurrentUserFriends(
                @RequestParam(value = "page", defaultValue = "0") int page,
                @PageableDefault(size = 20) Pageable pageable,
                @PathVariable("id") Long id) {
        
        Slice<FriendResponseDto> friends = friendService.findFriendByCurrentUser(id, pageable);
        return ResponseEntity.ok(friends);
    }

}
