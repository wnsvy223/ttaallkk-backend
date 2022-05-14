package security.ttaallkk.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.extern.log4j.Log4j2;
import security.ttaallkk.common.XssFilter;

@Configuration
@Log4j2
public class WebMvcConfig implements WebMvcConfigurer{

    private final long MAX_AGE_SECS = 3600;

    @Value("${origin.signal-url}")
    private String signalUrl;

    @Value("${origin.front-url}")
    private String frontUrl;
    
    @Value("${upload.image.location.root}")
    private String rootPath;

    @Value("${upload.image.location.profile}")
    private String profileStoragePath;

    @Value("${upload.image.location.post}")
    private String postStoragePath;

    //XSS 필터 등록
    @Bean
	public FilterRegistrationBean<XssFilter> getFilterRegistrationBean() {
		FilterRegistrationBean<XssFilter> registrationBean = new FilterRegistrationBean<>(new XssFilter());
		registrationBean.setOrder(Integer.MIN_VALUE);
		registrationBean.addUrlPatterns("/api/user/signUp", "/api/post/*", "/api/comment/*"); // xss필터링 되어야 할 앤드포인트(나중에 필요한 앤드포인트 추가)
		return registrationBean;
	}

	/**
     * 전역 CORS 설정
     */
	@Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(signalUrl, frontUrl)
                .allowCredentials(true)
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*")
                .exposedHeaders("X-Custom-Uid") // 사용자 정보 기반 공통 조회 로직에 사용될 커스텀 헤더 허용
				.maxAge(MAX_AGE_SECS);
    }

    /**
     * 리소스 경로 설정 및 캐시 설정
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler(
                    "/file/**",
                    "/profile/**",
                    "/post/**")
                .addResourceLocations(
                    "file:///" + rootPath,
                    "file:///" + profileStoragePath,
                    "file:///" + postStoragePath)
                .setCacheControl(CacheControl.maxAge(Duration.ofHours(1L)).cachePublic());
    }
}
