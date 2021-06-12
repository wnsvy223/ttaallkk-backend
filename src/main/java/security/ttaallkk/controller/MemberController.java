package security.ttaallkk.controller;

import lombok.RequiredArgsConstructor;
import security.ttaallkk.domain.Member;
import security.ttaallkk.dto.LoginDto;
import security.ttaallkk.dto.RefreshTokenDto;
import security.ttaallkk.dto.SignUpDto;
import security.ttaallkk.dto.response.LoginResponse;
import security.ttaallkk.dto.response.Response;
import security.ttaallkk.security.JwtProvider;
import security.ttaallkk.service.MemberSearchService;
import security.ttaallkk.service.MemberService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.Valid;

@Controller
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {

    private final Logger log = LoggerFactory.getLogger(MemberController.class);
    private final MemberService memberService;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;

    @Autowired
    private MemberSearchService memberSearchService;

    
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
            .message("회원가입성공")
            .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 회원 탈퇴
     * @param email
     * @return Response
     */
    @PostMapping("/signOut")
    public ResponseEntity<Response> deleteUser(@Valid @RequestBody LoginDto loginDto){
        memberService.signOut(loginDto.getEmail(), loginDto.getPassword());
        Response response = Response.builder()
            .status(HttpStatus.OK.value())
            .message("회원탈퇴성공")
            .build();
        return ResponseEntity.ok(response);
    }

    /**
     * 로그인
     * @param loginDto
     * @return LoginResponse
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginDto loginDto) {

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());

        //아이디 체크는 Authentication 에 사용자 입력 아이디, 비번을 넣어줘야지 작동
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        log.info(authentication + " 로그인 처리 authentication");

        //jwt accessToken & refreshToken 발급
        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        //회원 DB에 refreshToken 저장
        memberService.findMemberAndSaveRefreshToken(authentication.getName(), refreshToken);

        LoginResponse response = LoginResponse.builder()
                .status(HttpStatus.OK.value())
                .message("로그인 성공")
                .accessToken(accessToken)
                .expiredAt(LocalDateTime.now().plusSeconds(jwtProvider.getAccessTokenValidMilliSeconds()/1000))
                .refreshToken(refreshToken)
                .issuedAt(LocalDateTime.now())
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * refreshToken 으로 accessToken 재발급
     * @param refreshTokenDto accessToken 재발급 요청 dto
     * @return LoginResponse
     */
    @PostMapping("/refreshToken")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody RefreshTokenDto refreshTokenDto) {
        LoginResponse response = memberService.refreshToken(refreshTokenDto);
        return ResponseEntity.ok(response);
    }

    /**
     * 유저 검색(HibernateSearch를 이용한 FullTextSearch)
     * @param keyword
     * @return SearchMemberResponse
     */
    @GetMapping("/search/{keyword}")
    public ResponseEntity<List<Member>> search(@PathVariable("keyword") String keyword){
        List<Member> searchMemebers = memberSearchService.searchMemberByEmailOrDisplayName(keyword);
        return ResponseEntity.ok(searchMemebers);
    }

    /**
     * 테스트
     * @return Response
     */
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/test")
    public ResponseEntity<Response> test() {
        Response response = Response.builder()
                .status(HttpStatus.OK.value())
                .message("테스트 성공")
                .build();
        return ResponseEntity.ok(response);
    }
}
