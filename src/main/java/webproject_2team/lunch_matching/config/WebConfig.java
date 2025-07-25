package webproject_2team.lunch_matching.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 관련 설정을 담당하는 클래스입니다.
 * 인터셉터를 등록하여 특정 요청에 대한 처리를 추가합니다.
 */
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthenticationInterceptor authenticationInterceptor;

    /**
     * 인터셉터를 등록합니다.
     * 모든 경로("/**")에 대해 AuthenticationInterceptor를 적용하며,
     * "/access-denied", "/login", "/logout" 경로는 인터셉터 적용에서 제외합니다.
     * @param registry 인터셉터 등록을 위한 InterceptorRegistry 객체
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authenticationInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/access-denied", "/login", "/logout");
    }
}
