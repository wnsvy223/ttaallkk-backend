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

    private final String secretKey;
    private final long accessTokenValidMilliSeconds;
    private final long refreshTokenValidMilliSeconds;
    private Key key;

    public JwtProvider(@Value("${jwt.secretKey}") String secretKey,
                       @Value("${jwt.accessToken-valid-seconds}")long accessTokenValidSeconds,
                       @Value("${jwt.refreshToken-valid-seconds}")long refreshTokenValidSeconds) {
        this.secretKey = secretKey;
        this.accessTokenValidMilliSeconds = accessTokenValidSeconds * 1000;
        this.refreshTokenValidMilliSeconds = refreshTokenValidSeconds * 1000;
    }

    /**
     * secretKey 암호화 초기화
     */
    @PostConstruct
    protected void init() {
        this.key = Keys.hmacShaKeyFor(this.secretKey.getBytes());
    }

    /**
     * jwt 생성
     * @param authentication UserDetailsService 에서 인증 성공된 User 의 값들이 담긴 객체
     * @param isRefreshToken accessToken refreshToken 구분
     * @return 생성된 토큰
     */
    public String generateToken(Authentication authentication, boolean isRefreshToken) {
        String authorities = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority())
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validateDay;
        if(isRefreshToken) validateDay = new Date(now + this.refreshTokenValidMilliSeconds);
        else validateDay = new Date(now + this.accessTokenValidMilliSeconds);

        return Jwts.builder()
                .setSubject(authentication.getName())
                .claim("roles", authorities)
                .signWith(key, SignatureAlgorithm.HS256)
                .setExpiration(validateDay)
                .compact();
    }

    /**
     * jwt 추출 데이터 Authentication 에 넣기
     * @param token 토큰값
     * @return 회원 정보 담긴 인증객체
     */
    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();

        String[] roles = claims.get("roles").toString().split(",");
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles).map(role -> new SimpleGrantedAuthority(role)).collect(Collectors.toList());

        return new UsernamePasswordAuthenticationToken(claims.getSubject(), "", authorities);
    }
    
    /**
     * 추출된 jwt 토큰 유효성 검증
     * @param token 토큰값
     * @return 유효한 토큰인지 검증 결과
     */
    public boolean isValidToken(String token){
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (TokenNotFoundException e) {
            log.error("토큰을 찾을 수 없습니다.");
            return false;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.");
            return false;
        } catch (ExpiredJwtException e) {
            log.error("만료된 토큰입니다.");
            return false;
        } catch (UnsupportedJwtException e) {
            log.error("지원하지 않는 토큰입니다.");
            return false;
        } catch (IllegalArgumentException  e) {
            log.error("JWT Claims 문자열이 잘못되었습니다.");
            return false;
        }
    }
}
