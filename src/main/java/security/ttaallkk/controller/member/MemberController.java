package security.ttaallkk.controller.member;

import lombok.RequiredArgsConstructor;
import security.ttaallkk.domain.member.Friend;
import security.ttaallkk.dto.request.DeviceTokenUpdateDto;
import security.ttaallkk.dto.request.LoginDto;
import security.ttaallkk.dto.request.MemeberUpdateDto;
import security.ttaallkk.dto.request.RefreshTokenDto;
import security.ttaallkk.dto.request.SignUpDto;
import security.ttaallkk.dto.response.LoginResponse;
import security.ttaallkk.dto.response.MemberResponsDto;
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
     * ?????? ??????
     * @param SignUpDto
     * @return Response
     */
    @PostMapping("/signUp")
    public ResponseEntity<Response> signUp(@Valid @RequestBody SignUpDto signUpDto) {
        log.info("???????????? : " + signUpDto.getEmail());
        memberService.signUp(signUpDto);
        Response response = Response.builder()
                .status(HttpStatus.CREATED.value())
                .message("??????????????? ?????????????????????.")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * ?????? ??????
     * @param email
     * @return Response
     */
    @PostMapping("/signOut")
    public ResponseEntity<Response> signOut(@Valid @RequestBody LoginDto loginDto) {
        memberService.signOut(loginDto.getEmail(), loginDto.getPassword());
        Response response = Response.builder()
                .status(HttpStatus.OK.value())
                .message("??????????????? ?????????????????????.")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * ?????????
     * @param loginDto
     * @return LoginResponse + accessToken cookie + refreshToken cookie
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginDto loginDto, HttpServletResponse httpServletResponse) {

        //????????? ????????? ??????
        LoginResponse loginResponse = memberService.login(loginDto, authenticationManager);

        //??????????????? + ?????????????????? + uid ?????? ??????
        httpServletResponse.addCookie(createTokenCookie("accessToken", loginResponse.getAccessToken(), accessTokenCookieExpiredTime));
        httpServletResponse.addCookie(createTokenCookie("refreshToken", loginResponse.getRefreshToken(), refreshTokenCookieExpiredTime));

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * ????????????
     * @param httpServletResponse
     * @return Response
     */
    @PostMapping("/logout")
    public ResponseEntity<Response> logout(HttpServletResponse httpServletResponse) {
        
        //???????????? ????????? ??????
        memberService.logout();

        //?????? ?????? ??????
        httpServletResponse.addCookie(createTokenCookie("accessToken", null, 0));
        httpServletResponse.addCookie(createTokenCookie("refreshToken", null, 0));

        Response response = Response.builder()
                .status(HttpStatus.CREATED.value())
                .message("??????????????? ???????????? ???????????????.")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * refreshToken ?????? accessToken ?????????
     * @param refreshTokenDto accessToken ????????? ?????? dto
     * @param refreshTokenFromCookie ??????????????? ???????????? ????????? ????????? ???????????? ??????
     * @return LoginResponse + accessToken cookie + refreshToken cookie
     */
    @PostMapping("/refreshToken")
    public ResponseEntity<LoginResponse> refreshToken(
                HttpServletResponse httpServletResponse,
                @RequestBody RefreshTokenDto refreshTokenDto,
                @CookieValue(value = "refreshToken") String refreshTokenFromCookie) {

        //?????? ????????? ????????? ??????
        LoginResponse loginResponse = memberService.refreshToken(refreshTokenDto, refreshTokenFromCookie);

        //??????????????? + ?????????????????? + uid ?????? ??????
        httpServletResponse.addCookie(createTokenCookie("accessToken", loginResponse.getAccessToken(), accessTokenCookieExpiredTime));
        httpServletResponse.addCookie(createTokenCookie("refreshToken", loginResponse.getRefreshToken(), refreshTokenCookieExpiredTime));

        return ResponseEntity.ok(loginResponse);
    }

    /**
     * uid??? ?????? ?????? ??????
     * @param uid
     * @return MemberResponsDto
     */
    @GetMapping("/{uid}")
    public ResponseEntity<MemberResponsDto> getMemberByUid(@PathVariable("uid") String uid) {
        
        MemberResponsDto memberResponsDto = memberService.findMemberByUid(uid);
        
        return ResponseEntity.ok(memberResponsDto);
    }

    /**
     * ?????? ????????? ?????? ????????????
     * @param uid ??????????????? ????????? ?????? ?????????
     * @param memeberUpdateDto ???????????? ??? ?????????
     * @return MemberUpdateResponseDto
     */
    @PutMapping("/{uid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MemberUpdateResponseDto> updateProfile(@RequestBody MemeberUpdateDto memeberUpdateDto, @PathVariable("uid") String uid) {
        
        MemberUpdateResponseDto memberUpdateResponseDto= memberService.updateProfile(memeberUpdateDto, uid);
        
        return ResponseEntity.ok(memberUpdateResponseDto);
    } 

    /**
     * ?????? ????????? ????????? ?????????
     * @param profileImage
     * @param uid
     * @return ????????? ????????? url
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
     * ???????????? ?????? ?????? ??? ??????
     * @param deviceTokenUpdateDto
     * @return Response
     */
    @PutMapping("/devicetoken")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response> updateDeviceToken(@RequestBody DeviceTokenUpdateDto deviceTokenUpdateDto) {
        memberService.updateDeviceToken(deviceTokenUpdateDto.getDeviceToken(), deviceTokenUpdateDto.getUid());

        Response response = Response.builder()
                .status(HttpStatus.OK.value())
                .message("???????????? ????????? ?????????????????????.")
                .build();
        return ResponseEntity.ok(response);
    }


    /**
     * ?????? ??????(HibernateSearch??? ????????? FullTextSearch) + ?????????
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
     * ?????? ????????? ?????? ??????
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
