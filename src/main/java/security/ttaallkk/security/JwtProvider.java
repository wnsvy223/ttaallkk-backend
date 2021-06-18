package security.ttaallkk.security;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import security.ttaallkk.exception.TokenNotFoundException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
@Getter
@Log4j2
public class JwtProvider{

    private final String secretKeyAccessToken; //엑세스 토큰 secretKey
    private final String secretKeyRefreshToken; //리프래시 토큰 secretKey
    private final long accessTokenValidMilliSeconds; //엑세스 토큰 만료시간
    private final long refreshTokenValidMilliSeconds; //리프래시 토큰 만료시간
    private Key keyAccessToken; //암호화된 엑세스 토큰 secretKey
    private Key keyRefreshToken; //암호화된 리프래시 토큰 secretKey 

    public JwtProvider(@Value("${jwt.secretKey-accessToken}") String secretKeyAccessToken,
                       @Value("${jwt.secretKey-refreshToken}") String secretKeyRefreshToken,
                       @Value("${jwt.accessToken-valid-seconds}")long accessTokenValidSeconds,
                       @Value("${jwt.refreshToken-valid-seconds}")long refreshTokenValidSeconds) {
        this.secretKeyAccessToken = secretKeyAccessToken;
        this.secretKeyRefreshToken = secretKeyRefreshToken;
        this.accessTokenValidMilliSeconds = accessTokenValidSeconds * 1000;
        this.refreshTokenValidMilliSeconds = refreshTokenValidSeconds * 1000;
    }

    /**
     * secretKey 암호화 초기화
     */
    @PostConstruct
    protected void init() {
        this.keyAccessToken = Keys.hmacShaKeyFor(this.secretKeyAccessToken.getBytes());
        this.keyRefreshToken = Keys.hmacShaKeyFor(this.secretKeyRefreshToken.getBytes());
    }

    /**
     * 엑세스 토큰 생성
     * @param authentication UserDetailsService 에서 인증 성공된 User의 값들이 담긴 객체
     * @return 생성된 엑세스 토큰
     */
    public String generateAccessToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.joining(","));
        long now = new Date().getTime();
        Date exitredTime = new Date(now + this.accessTokenValidMilliSeconds);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("roles", authorities)
                .signWith(keyAccessToken, SignatureAlgorithm.HS256)
                .setExpiration(exitredTime)
                .compact();
    }

    /**
     * 리프래시 토큰 생성
     * @param authentication UserDetailsService 에서 인증 성공된 User의 값들이 담긴 객체
     * @return 생성된 리프래시 토큰
     */
    public String generateRefreshToken(Authentication authentication) {
        String authorities = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.joining(","));
        long now = new Date().getTime();
        Date exitredTime = new Date(now + this.refreshTokenValidMilliSeconds);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("roles", authorities)
                .signWith(keyRefreshToken, SignatureAlgorithm.HS256)
                .setExpiration(exitredTime)
                .compact();
    }

    /**
     * 엑세스 토큰 추출 데이터 Authentication 에 넣기
     * @param accessToken 엑세스 토큰
     * @return 회원 정보 담긴 인증객체
     */
    public Authentication getAuthentication(String accessToken) {
        Claims claims = Jwts.parserBuilder().setSigningKey(keyAccessToken).build().parseClaimsJws(accessToken).getBody();

        String[] roles = claims.get("roles").toString().split(",");
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles).map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
    }
    
    /**
     * 리프래시 토큰 추출 데이터 Authentication 에 넣기
     * @param refreshToken 리프래시 토큰
     * @return 회원 정보 담긴 인증객체
     */
    public Authentication getAuthenticationFromRefreshToken(String refreshToken) {
        Claims claims = Jwts.parserBuilder().setSigningKey(keyRefreshToken).build().parseClaimsJws(refreshToken).getBody();

        String[] roles = claims.get("roles").toString().split(",");
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles).map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
    }
    
    /**
     * 추출된 엑세스 토큰 유효성 검증
     * @param accessToken 엑세스 토큰값
     * @return 유효한 엑세스 토큰인지 검증 결과
     */
    public boolean isValidToken(String accessToken){
        try {
            Jwts.parserBuilder().setSigningKey(keyAccessToken).build().parseClaimsJws(accessToken);
            return true;
        } catch (TokenNotFoundException e) {
            log.error("토큰을 찾을 수 없습니다.");
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.error("만료된 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 토큰입니다.");
        } catch (IllegalArgumentException  e) {
            log.error("JWT Claims 문자열이 잘못되었습니다.");
        }
        return false;
    }
}
