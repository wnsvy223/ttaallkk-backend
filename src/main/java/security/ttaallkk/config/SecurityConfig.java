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
     * AuthenticationManager ??? ???????????? ???????????? ?????? @Bean ??????
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth
            .userDetailsService(memberService) //????????? userDetailService??????
            .passwordEncoder(passwordEncoder); //AppConfig??? ????????? ????????? ?????????passwordEncoder??????
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            .httpBasic()
                .disable() //rest api????????? ???????????? ?????????
            .formLogin()
                .disable() //form ????????? ???????????? ?????? ???????????? ??????.
            .cors()
                .and() //CORS ?????? corsConfigurationSource????????? ????????????.
            .csrf()
                .disable() //CSRF ????????????
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS) //????????? ???????????? ????????? ???????????? ???????????? STATELESS??? ??????.
                .and()
            .anonymous()
                .authorities("ROLE_ANONYMOUS") //?????? ???????????? ROLE_ANONYMOUS?????? ??????
                .and()
            .authorizeRequests()
                .antMatchers(HttpMethod.POST, "/api/user/signUp", "/api/user/login", "/api/user/refreshToken", "/api/post/**", "/api/comment/**", "/api/like/**", "/api/unlike/**","/api/category/**", "/api/friend/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/user/search/**", "/api/post/**", "/api/comment/**", "/api/like/**", "/api/unlike/**", "/api/category/**", "/api/friend/**").permitAll()
                .antMatchers(HttpMethod.DELETE, "/api/post/**", "/api/comment/**", "/api/like/**", "/api/unlike/**", "/api/category/**", "/api/friend/**").permitAll()
                .antMatchers(HttpMethod.PUT, "/api/post/**", "/api/comment/**", "/api/like/**", "/api/unlike/**", "/api/category/**", "/api/friend/**").permitAll()
                .antMatchers(HttpMethod.GET, "/profile/**", "/post/**").permitAll() //????????? ??? ????????? ????????? ?????? ??????????????? ?????? ??????
                .anyRequest().authenticated() //??? ????????? ?????????????????? ????????????.
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
            .requestMatchers(PathRequest.toStaticResources().atCommonLocations()); //?????? ????????? ???????????? Security ????????? ???????????? ??????.
    }

    /**
     * Security CORS ??????
     */
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.applyPermitDefaultValues();
        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        corsConfiguration.setAllowedOriginPatterns(Arrays.asList(signalUrl, frontUrl)); //??????????????? CORS ????????? ??????(setAllowCredentials?????? ?????? ??? setAllowedOriginPatterns ???????????? ???????????????)
        corsConfiguration.setAllowCredentials(true);
        corsConfiguration.addExposedHeader("Authorization"); //Bearer?????? ????????? ?????? Authorization?????? ??????
        source.registerCorsConfiguration("/**", corsConfiguration);
        return source;
    }
}
