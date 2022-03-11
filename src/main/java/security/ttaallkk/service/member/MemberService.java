package security.ttaallkk.service.member;
import lombok.RequiredArgsConstructor;
import security.ttaallkk.domain.member.Member;
import security.ttaallkk.domain.member.MemberRole;
import security.ttaallkk.dto.request.LoginDto;
import security.ttaallkk.dto.request.MemeberUpdateDto;
import security.ttaallkk.dto.request.RefreshTokenDto;
import security.ttaallkk.dto.request.SignUpDto;
import security.ttaallkk.dto.response.LoginResponse;
import security.ttaallkk.dto.response.MemberUpdateResponseDto;
import security.ttaallkk.exception.AuthenticatedFailureException;
import security.ttaallkk.exception.DisplayNameAlreadyExistException;
import security.ttaallkk.exception.EmailAlreadyExistException;
import security.ttaallkk.exception.InvalidRefreshTokenException;
import security.ttaallkk.exception.PasswordNotMatchException;
import security.ttaallkk.exception.RefreshTokenGrantTypeException;
import security.ttaallkk.repository.member.MemberRepository;
import security.ttaallkk.repository.post.PostRepository;
import security.ttaallkk.security.JwtProvider;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final PostRepository postRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    @Value("${admin.email}")
    private String adminEmail;
    
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
        if(signUpDto.getEmail().equals(adminEmail)){
            roles.add(MemberRole.ADMIN);
        }else{
            roles.add(MemberRole.USER);
        }
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
            removeAllPostBySignOutMember(member.getUid());
            memberRepository.deleteByEmail(email);
        }else{
            throw new PasswordNotMatchException();
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
                    .expiredAtAccessToken(LocalDateTime.now().plusSeconds(jwtProvider.getAccessTokenValidMilliSeconds() / 1000))
                    .expiredAtRefereshToken(LocalDateTime.now().plusSeconds(jwtProvider.getRefreshTokenValidMilliSeconds() / 1000))
                    .refreshToken(refreshToken)
                    .issuedAt(LocalDateTime.now())
                    .build();
                         
            return response;
        }else{
            throw new AuthenticatedFailureException();
        }
    }

    /**
     * 로그아웃(현재 인증 유저의 리프래시 토큰 DB에서 삭제)
     */
    @Transactional
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Member member = memberRepository.findMemberByEmail(authentication.getName())
            .orElseThrow(() -> new UsernameNotFoundException(authentication.getName() + " 이메일이 일치하지 않습니다"));

        member.updateRefreshToken(null); //리프래시 토큰 삭제
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

        //요청정보에서 추출한 리프래시토큰과 사용자 이메일로 DB에서 정보조회. 값이 다를경우 유효하지 않은 리프래시 토큰이므로 예외처리 발생
        Member member = memberRepository.findMemberByEmailAndRefreshToken(authentication.getName(), refreshTokenFromCookie)
                .orElseThrow(() -> new InvalidRefreshTokenException("유효하지 않은 리프래시 토큰입니다")); //InvalidRefreshTokenException 예외 Handler

        //jwt accessToken & refreshToken 발급
        String accessToken = jwtProvider.generateAccessToken(authentication);
        String refreshToken = jwtProvider.generateRefreshToken(authentication);

        //refreshToken 업데이트 (기존 refreshToken은 한번 사용후 폐기)
        member.updateRefreshToken(refreshToken);

        LoginResponse response = LoginResponse.builder()
                .status(HttpStatus.OK.value())
                .message("accessToken 재발급 성공")
                .email(member.getEmail())
                .uid(member.getUid())
                .displayName(member.getDisplayName())
                .profileUrl(member.getProfileUrl())
                .accessToken(accessToken)
                .expiredAtAccessToken(LocalDateTime.now().plusSeconds(jwtProvider.getAccessTokenValidMilliSeconds() / 1000))
                .expiredAtRefereshToken(LocalDateTime.now().plusSeconds(jwtProvider.getRefreshTokenValidMilliSeconds() / 1000))
                .refreshToken(refreshToken)
                .issuedAt(LocalDateTime.now())
                .build();

        return response;
    }

    /**
     * 유저 프로필 업데이트
     * @param MemeberUpdateDto //업데이트 요청 객체(displayName + profileUrl)
     * @param uid //사용자 고유 uid
     */
    @Transactional
    public MemberUpdateResponseDto updateProfile(MemeberUpdateDto memeberUpdateDto, String uid) {
        validateDuplicateUserByDisplayName(memeberUpdateDto.getDisplayName()); //닉네임 중복체크

        Member member = memberRepository.findMemberByUid(uid)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 정보를 찾을 수 없습니다."));

        member.updateProfile(memeberUpdateDto.getDisplayName(), memeberUpdateDto.getProfileUrl());

        MemberUpdateResponseDto memberUpdateResponseDto = MemberUpdateResponseDto.builder()
                .status(200)
                .message("프로필 업데이트 성공")
                .uid(member.getUid())
                .email(member.getEmail())
                .displayName(member.getDisplayName())
                .profileUrl(member.getProfileUrl())
                .build();

        return memberUpdateResponseDto;
    }

    /**
     * 회원가입 이메일 중복체크
     * @param email
     * @throws EmailAlreadyExistException
     */
    private void validateDuplicateUserByEmail(String email) {
        if(memberRepository.existsByEmail(email)) throw new EmailAlreadyExistException("이미 존재하는 이메일 입니다. 새로운 이메일로 시도해 보세요.");
    }

    /**
     * 회원가입 닉네임 중복체크
     * @param displayName
     * @throws DisplayNameAlreadyExistException
     */
    private void validateDuplicateUserByDisplayName(String displayName) {
        if(memberRepository.existsByDisplayName(displayName)) throw new DisplayNameAlreadyExistException("이미 존재하는 닉네임 입니다. 새로운 닉네임으로 시도해 보세요.");
    }

    /**
     * 회원탈퇴 시 해당 사용자가 작성한 글도 모두 삭제
     * @param uid
     */
    private void removeAllPostBySignOutMember(String uid){
        postRepository.deleteAllPostByUid(uid);
    }
}
