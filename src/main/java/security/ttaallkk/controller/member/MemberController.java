package security.ttaallkk.controller.member;

import lombok.RequiredArgsConstructor;
import security.ttaallkk.domain.member.Friend;
import security.ttaallkk.dto.request.DeviceTokenUpdateDto;
import security.ttaallkk.dto.request.LoginDto;
import security.ttaallkk.dto.request.MemeberUpdateDto;
import security.ttaallkk.dto.request.RefreshTokenDto;
import security.ttaallkk.dto.request.SignUpDto;
import security.ttaallkk.dto.response.LoginResponse;
import security.ttaallkk.dto.response.MemberSearchResponseDto;
import security.ttaallkk.dto.response.MemberUpdateResponseDto;
import security.ttaallkk.dto.response.Response;
import security.ttaallkk.service.member.FriendService;
import security.ttaallkk.service.member.MemberSearchService;
import security.ttaallkk.service.member.MemberService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class MemberController {

    @Value("${jwt.accessToken-valid-seconds}")
    private int accessTokenCookieExpiredTime;

    @Value("${jwt.refreshToken-valid-seconds}")
    private int refreshTokenCookieExpiredTime;
    
    private final Logger log = LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;
    private final AuthenticationManager authenticationManager;
    private final MemberSearchService memberSearchService;
    private final FriendService friendService;

    
    /**
     * 회원 가입
     * @param SignUpDto
     * @return Response
     */
    @PostMapping("/signUp")
    public ResponseEntity<Response> signUp(@Valid @RequestBody SignUpDto signUpDto) {
        log.info("회원가입 : " + signUpDto.getEmail());
        memberService.signUp(signUpDto);
        Response response = Response.builder()
                .status(HttpStatus.CREATED.value())
                .message("회원가입이 완료되었습니다.")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 회원 탈퇴
     * @param email
     * @return Response
     */
    @PostMapping("/signOut")
    public ResponseEntity<Response> signOut(@Valid @RequestBody LoginDto loginDto) {
        memberService.signOut(loginDto.getEmail(), loginDto.getPassword());
        Response response = Response.builder()
                .status(HttpStatus.OK.value())
                .message("회원탈퇴가 완료되었습니다.")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 로그인
     * @param loginDto
     * @return LoginResponse + accessToken cookie + refreshToken cookie
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginDto loginDto, HttpServletResponse httpServletResponse) {

        //로그인 서비스 로직
        LoginResponse loginResponse = memberService.login(loginDto, authenticationManager);

        //엑세스토큰 + 리프래시토큰 + uid 쿠키 생성
        httpServletResponse.addCookie(createTokenCookie("accessToken", loginResponse.getAccessToken(), accessTokenCookieExpiredTime));
        httpServletResponse.addCookie(createTokenCookie("refreshToken", loginResponse.getRefreshToken(), refreshTokenCookieExpiredTime));

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * 로그아웃
     * @param httpServletResponse
     * @return Response
     */
    @PostMapping("/logout")
    public ResponseEntity<Response> logout(HttpServletResponse httpServletResponse) {
        
        //로그아웃 서비스 로직
        memberService.logout();

        //쿠키 정보 제거
        httpServletResponse.addCookie(createTokenCookie("accessToken", null, 0));
        httpServletResponse.addCookie(createTokenCookie("refreshToken", null, 0));

        Response response = Response.builder()
                .status(HttpStatus.CREATED.value())
                .message("성공적으로 로그아웃 되었습니다.")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * refreshToken 으로 accessToken 재발급
     * @param refreshTokenDto accessToken 재발급 요청 dto
     * @param refreshTokenFromCookie 클라이언트 쿠키에서 전송된 기존의 리프래시 토큰
     * @return LoginResponse + accessToken cookie + refreshToken cookie
     */
    @PostMapping("/refreshToken")
    public ResponseEntity<LoginResponse> refreshToken(
                HttpServletResponse httpServletResponse,
                @RequestBody RefreshTokenDto refreshTokenDto,
                @CookieValue(value = "refreshToken") String refreshTokenFromCookie) {

        //토큰 재발급 서비스 로직
        LoginResponse loginResponse = memberService.refreshToken(refreshTokenDto, refreshTokenFromCookie);

        //엑세스토큰 + 리프래시토큰 + uid 쿠키 생성
        httpServletResponse.addCookie(createTokenCookie("accessToken", loginResponse.getAccessToken(), accessTokenCookieExpiredTime));
        httpServletResponse.addCookie(createTokenCookie("refreshToken", loginResponse.getRefreshToken(), refreshTokenCookieExpiredTime));

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * 유저 프로필 정보 업데이트
     * @param uid 업데이트할 유저의 고유 아이디
     * @param memeberUpdateDto 업데이트 할 데이터
     * @return MemberUpdateResponseDto
     */
    @PutMapping("/{uid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MemberUpdateResponseDto> updateProfile(@RequestBody MemeberUpdateDto memeberUpdateDto, @PathVariable("uid") String uid) {
        
        MemberUpdateResponseDto memberUpdateResponseDto= memberService.updateProfile(memeberUpdateDto, uid);
        
        return ResponseEntity.ok(memberUpdateResponseDto);
    } 

    /**
     * 유저 프로필 이미지 업로드
     * @param profileImage
     * @param uid
     * @return 프로필 이미지 url
     */
    @PostMapping("/{uid}/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> uploadUserProfileImage(
            @RequestParam(value = "files") MultipartFile multipartFile,
            @PathVariable("uid") String uid) {
        
        String downloadUrl = memberService.updateProfileUrl(multipartFile, uid);

        return new ResponseEntity<>(downloadUrl, HttpStatus.OK); 
    }

    /**
     * 디바이스 토큰 등록 및 갱신
     * @param deviceTokenUpdateDto
     * @return Response
     */
    @PutMapping("/devicetoken")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response> updateDeviceToken(@RequestBody DeviceTokenUpdateDto deviceTokenUpdateDto) {
        memberService.updateDeviceToken(deviceTokenUpdateDto.getDeviceToken(), deviceTokenUpdateDto.getUid());

        Response response = Response.builder()
                .status(HttpStatus.OK.value())
                .message("디바이스 토큰이 등록되었습니다.")
                .build();
        return ResponseEntity.ok(response);
    }


    /**
     * 유저 검색(HibernateSearch를 이용한 FullTextSearch) + 페이징
     * @param keyword
     * @param page
     * @param pageable
     * @param uid
     * @return Slice<MemberSearchResponseDto>
     */
    @GetMapping("/search")
    public ResponseEntity<Slice<MemberSearchResponseDto>> searchMember(
                @RequestParam(value = "keyword") String keyword,
                @RequestParam(value = "page", defaultValue = "0") int page,
                @PageableDefault(size = 20) Pageable pageable,
                @RequestHeader("X-Custom-Uid") String uid) {
        
        List<Friend> friends = friendService.findFromOrToByUid(uid);
        Slice<MemberSearchResponseDto> searchMembers = memberSearchService.searchMemberByEmailOrDisplayName(keyword, pageable, friends);
        
        return ResponseEntity.ok(searchMembers);
    }


    /**
     * 토큰 저장된 쿠키 발급
     * @param key
     * @param value
     * @param maxAge
     * @return cookie
     */
    private Cookie createTokenCookie(String key, String value, int maxAge) {
        Cookie cookie = new Cookie(key, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(maxAge);
        cookie.setPath("/");
        return cookie;
    }
}
