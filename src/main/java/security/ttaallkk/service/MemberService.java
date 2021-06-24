package security.ttaallkk.service;
import lombok.RequiredArgsConstructor;
import security.ttaallkk.domain.Member;
import security.ttaallkk.domain.MemberRole;
import security.ttaallkk.dto.LoginDto;
import security.ttaallkk.dto.RefreshTokenDto;
import security.ttaallkk.dto.SignUpDto;
import security.ttaallkk.dto.response.LoginResponse;
import security.ttaallkk.exception.DisplayNameAlreadyExistException;
import security.ttaallkk.exception.EmailAlreadyExistException;
import security.ttaallkk.exception.InvalidRefreshTokenException;
import security.ttaallkk.exception.PasswordNotMatchException;
import security.ttaallkk.exception.RefreshTokenGrantTypeException;
import security.ttaallkk.repository.MemberRepository;
import security.ttaallkk.security.JwtProvider;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService implements UserDetailsService {

    private final Logger log = LoggerFactory.getLogger(MemberService.class);
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    
    /**
     * Security에서 제공되는 로그인 요청 회원 조회 메소드(Security인증매니저의 인증로직 수행 시 호출)
     * @param email 요청 이메일
     * @return 회원 정보 넣은 security User 객체
     * @throws UsernameNotFoundException
     */
    @Transactional(readOnly = true)
    @Override
    public UserDetails loadUserByUsername(String email) {
        log.info("로그인 요청 회원 찾기" + email);
        Member member = memberRepository.findMemberByEmailFetch(email)
                .orElseThrow(() -> new UsernameNotFoundException(email + " 이메일이 일치하지 않습니다"));

        return new User(member.getEmail(), member.getPassword(), authorities(member.getRoles()));
    }

    private Collection<? extends GrantedAuthority> authorities(Set<MemberRole> roles) {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());
    }

    /**
     * 회원 가입
     * @param signUpDto 회원가입용 데이터 객체(Email + Password + DisplayName)
     */
    @Transactional
    public void signUp(SignUpDto signUpDto) {
        validateDuplicateUserByEmail(signUpDto.getEmail()); //이메일 중복가입 체크
        validateDuplicateUserByDisplayName(signUpDto.getDisplayName()); //닉네임 중복가입 체크
        Set<MemberRole> roles = new HashSet<>();
        //roles.add(MemberRole.ADMIN);
        roles.add(MemberRole.USER);
        Member member = Member.builder()
                .email(signUpDto.getEmail())
                .password(passwordEncoder.encode(signUpDto.getPassword()))
                .displayName(signUpDto.getDisplayName())
                .roles(roles)
                .uid(RandomStringUtils.random(30, 32, 127, true, true))
                .build();

        memberRepository.save(member);
    }

    /**
     * 회원 탈퇴
     * @param email 이메일
     * @param password 비밀번호(비밀번호를 다시 체크하여 맞는다면 회원탈퇴) 
     * @exception UsernameNotFoundException
     * @exception PasswordNotMatchException
     */
    @Transactional
    public void signOut(String email, String password) {
        Member member = memberRepository.findMemberByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException(email + " 이메일이 일치하지 않습니다"));
        if(passwordEncoder.matches(password, member.getPassword())){
            memberRepository.deleteByEmail(email);
        }else{
            throw new PasswordNotMatchException("비밀번호가 틀렸습니다.");
        }    
    }

    /**
     * 로그인
     * @param loginDto 로그인용 데이터 객체(Email + Password)
     * @param AuthenticationManager 로그인 비지니스 로직을 서비스에서 처리하기 위해 컨트롤러로부터 받아온 인증매니저 객체
     * @return 로그인 인증된 사용자 정보 객체
     */
    @Transactional
    public LoginResponse login(LoginDto loginDto, AuthenticationManager authenticationManager) {
        //이메일과 비밀번호 기반으로 사용자 인증토큰 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());

        //인증토큰으로 사용자 객체 생성
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        log.info(authentication + " 로그인 처리 authentication");

        if(authentication.isAuthenticated()){ 
            //jwt accessToken & refreshToken 발급
            String accessToken = jwtProvider.generateAccessToken(authentication);
            String refreshToken = jwtProvider.generateRefreshToken(authentication);

            //인증된 athentication객체로 회원 정보 조회
            Member member = memberRepository.findMemberByEmail(authentication.getName())
                    .orElseThrow(() -> new UsernameNotFoundException(authentication.getName() + "이메일이 일치하지 않습니다."));
            
            //회원 DB에 refreshToken 저장
            member.updateRefreshToken(refreshToken);

            //커스텀 로그인 응답 DTO 생성
            LoginResponse response = LoginResponse.builder()
                    .status(HttpStatus.OK.value())
                    .message("로그인 성공")
                    .email(member.getEmail())
                    .uid(member.getUid())
                    .displayName(member.getDisplayName())
                    .profileUrl(member.getProfileUrl())
                    .accessToken(accessToken)
                    .expiredAt(LocalDateTime.now().plusSeconds(jwtProvider.getAccessTokenValidMilliSeconds()/1000))
                    .refreshToken(refreshToken)
                    .issuedAt(LocalDateTime.now())
                    .build();
                         
            return response;
        }else{
            return null;
        }
    }


    /**
     * 회원DB에 refreshToken 저장
     * @param email 요청 이메일
     * @param refreshToken refreshToken 값
     * @exception UsernameNotFoundException
     */
    @Transactional
    public void findMemberAndSaveRefreshToken(String email, String refreshToken) {
        Member member = memberRepository.findMemberByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email + " 이메일이 일치하지 않습니다"));
        member.updateRefreshToken(refreshToken);
    }

    /**
     * refreshToken 으로 accessToken 재발급
     * @param refreshTokenDto accessToken 재발급 요청 dto
     * @return 갱신된 리프래시 토큰 + 엑세스 토큰을 포함한 사용자 정보 객체
     */
    @Transactional
    public LoginResponse refreshToken(RefreshTokenDto refreshTokenDto, String refreshTokenFromCookie) {
        if (!refreshTokenDto.getGrantType().equals("refreshToken"))
            throw new RefreshTokenGrantTypeException("올바른 grantType 을 입력해주세요");
        Authentication authentication = jwtProvider.getAuthenticationFromRefreshToken(refreshTokenFromCookie);

        Member member = memberRepository.findMemberByEmailAndRefreshToken(authentication.getName(), refreshTokenFromCookie)
                .orElseThrow(() -> new InvalidRefreshTokenException("유효하지 않은 리프래시 토큰입니다")); //InvalidRefreshTokenException 예외 Handler

        //jwt accessToken & refreshToken 발급
        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        //refreshToken 저장 (refreshToken 은 한번 사용후 폐기)
        member.updateRefreshToken(refreshToken);

        LoginResponse response = LoginResponse.builder()
                .status(HttpStatus.OK.value())
                .message("accessToken 재발급 성공")
                .email(member.getEmail())
                .uid(member.getUid())
                .displayName(member.getDisplayName())
                .profileUrl(member.getProfileUrl())
                .accessToken(accessToken)
                .expiredAt(LocalDateTime.now().plusSeconds(jwtProvider.getAccessTokenValidMilliSeconds()/1000))
                .refreshToken(refreshToken)
                .issuedAt(LocalDateTime.now())
                .build();

        return response;
    }

    /**
     * 회원가입 이메일 중복체크
     * @param email
     * @throws EmailAlreadyExistException
     */
    private void validateDuplicateUserByEmail(String email) {
        if(memberRepository.findMemberByEmail(email).isPresent()) throw new EmailAlreadyExistException("이미 가입된 사용자 이메일 입니다. 새로운 이메일로 가입을 진행하세요.");
    }

    /**
     * 회원가입 닉네임 중복체크
     * @param displayName
     * @throws DisplayNameAlreadyExistException
     */
    private void validateDuplicateUserByDisplayName(String displayName) {
        if(memberRepository.findMemberByDisplayName(displayName).isPresent()) throw new DisplayNameAlreadyExistException("이미 가입된 사용자 닉네임 입니다. 새로운 닉네임으로 가입을 진행하세요.");
    }
}
