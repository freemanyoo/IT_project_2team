package webproject_2team.lunch_matching.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Log4j2
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("=====SecurityConfig=====");

        // CSRF임.
        // REST API 개발중에는 잠시 비활성화
        http.csrf(AbstractHttpConfigurer::disable);

        // 특정 경로에 대한 접근 권한 설정
        http.authorizeHttpRequests(authorize -> authorize
                                // 1. 가장 먼저, 모든 정적 리소스와 기본 페이지에 대한 접근을 허용합니다.**
                                // PathRequest.toStaticResources().atCommonLocations() 대신 더 명시적인 AntPathRequestMatcher 사용
                                .requestMatchers(
                                        "/", // 루트 경로
                                        "/signup.html", // 회원가입 HTML 파일 자체
                                        "/js/**",       // 모든 JavaScript 파일 (예: /js/signup.js)
                                        "/css/**",      // 모든 CSS 파일
                                        "/images/**",   // 모든 이미지 파일 (만약 /images 폴더를 사용한다면)
                                        "/favicon.ico"  // 파비콘
                                ).permitAll()

                // 회원가입, 이메일 인증 API는 인증 없이 접근 허용
                // 이렇게 하면 `/api/members/signup`, `/api/auth/send-code` 등 모두 포함됩니다.
                .requestMatchers("/api/members/**", "/api/auth/**").permitAll()


                // H2 Console, Swagger UI, 업로드 파일 접근은 인증 없이 허용
                .requestMatchers("/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**", "/upload/**", "/error/**").permitAll()
                // 그 외 모든 요청은 인증 필요 (로그인 필요)
                .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/login") // 로그인 페이지 URL 설정 (실제 로그인 페이지 경로로 변경 필요)
                        .permitAll() // 로그인 페이지는 인증 없이 접근 허용
                )
                .logout(logout -> logout
                        .permitAll() // 로그아웃은 인증 없이 접근 허용
        );
        // H2 Console 프레임 허용 (개발 환경에서만 필요)
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        // TODO: 추후 로그인, 로그아웃, 예외 처리 등을 추가

        return http.build();
    }
}