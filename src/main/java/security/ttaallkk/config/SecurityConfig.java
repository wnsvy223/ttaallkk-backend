package security.ttaallkk.config;

import java.util.Arrays;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import security.ttaallkk.security.JwtAccessDeniedHandler;
import security.ttaallkk.security.JwtAuthenticationEntryPoint;
import security.ttaallkk.security.JwtFilter;
import security.ttaallkk.security.JwtProvider;
import security.ttaallkk.service.member.MemberService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Value("${origin.signal-url}")
    private String signalUrl;

    @Value("${origin.front-url}")
    private String frontUrl;

    private final MemberService memberService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAccessDeniedHandler jwtAccessDeniedHandler;

    /**
     * AuthenticationManager 을 외부에서 사용하기 위해 @Bean 등록
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .userDetailsService(memberService) //커스텀 userDetailService사용
            .passwordEncoder(passwordEncoder); //AppConfig에 빈으로 정의한 커스텀passwordEncoder사용
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .httpBasic()
                .disable() //rest api이므로 기본설정 미사용
            .formLogin()
                .disable() //form 기반의 로그인에 대해 비활성화 한다.
            .cors()
                .and() //CORS 설정 corsConfigurationSource빈에서 세부설정.
            .csrf()
                .disable() //CSRF 비활성화
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) //토큰을 활용하면 세션을 사용하지 않으므로 STATELESS로 설정.
                .and()
            .anonymous()
                .authorities("ROLE_ANONYMOUS") //익명 유저에게 ROLE_ANONYMOUS권한 부여
                .and()
            .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/user/signUp", "/api/user/login", "/api/user/refreshToken", "/api/post/**", "/api/comment/**", "/api/like/**", "/api/unlike/**","/api/category/**").permitAll()//회원가입, 로그인, 리프래시토큰발급 앤드포인트는 인증없이 허용.
                .antMatchers(HttpMethod.GET, "/api/user/search/**", "/api/post/**", "/api/comment/**", "/api/like/**", "/api/unlike/**", "/api/category/**").permitAll() //유저 검색 앤드포인트 허용
                .antMatchers(HttpMethod.DELETE, "/api/post/**", "/api/comment/**", "/api/like/**", "/api/unlike/**", "/api/category/**").permitAll()
                .antMatchers(HttpMethod.PUT, "/api/post/**", "/api/comment/**", "/api/like/**", "/api/unlike/**", "/api/category/**").permitAll()
                .anyRequest().authenticated() //그 이외의 앤드포인트는 인증필요.
                .and()    
            .exceptionHandling()
                .accessDeniedHandler(jwtAccessDeniedHandler)
                .and()
            .exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .and()
            .addFilterBefore(new JwtFilter(jwtProvider, objectMapper), UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web
            .ignoring()
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()); //정적 자원에 대해서는 Security 설정을 적용하지 않음.
    }

    /**
     * Security CORS 설정
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.applyPermitDefaultValues();
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        corsConfiguration.setAllowedOriginPatterns(Arrays.asList(signalUrl, frontUrl)); //패턴방식의 CORS 오리진 허용(setAllowCredentials옵션 사용 시 setAllowedOriginPatterns 메소드로 설정해야함)
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addExposedHeader("Authorization"); //Bearer토큰 사용을 위해 Authorization헤더 허용
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}
