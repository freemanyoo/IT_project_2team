package webproject_2team.lunch_matching.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService; // UserDetailsService 임포트
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl; // JDBC 기반 remember-me
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository; // JDBC 기반 remember-me 인터페이스

import javax.sql.DataSource;


@Configuration
@EnableWebSecurity
@Log4j2
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService; // MemberDetailsService 주입
    private final DataSource dataSource; // JDBC 기반 remember-me를 위해 DataSource 주입

    // WebSecurityCustomizer를 사용하여 특정 요청을 보안 필터 체인에서 완전히 제외
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        log.info("=====WebSecurityCustomizer=====");
        return (web) -> web.ignoring()
                // 정적 리소스 경로를 보안 필터 체인에서 완전히 무시
                .requestMatchers(
                        "/", // 루트 경로도 포함 (혹시 모를 경우)
                        "/signup.html", // 회원가입 HTML 파일 자체
                        "/test",
                        "/static/js/**",       // 모든 JavaScript 파일
                        "/css/**",      // 모든 CSS 파일
                        "/images/**",   // 모든 이미지 파일
                        "/favicon.ico", // 파비콘
                        "/error/**",     // 에러 페이지도 무시 (선택 사항)
                        "/.well-known/**" // 크롬 개발자 도구 관련 경로 무시
                )
                // PathRequest.toStaticResources().atCommonLocations()도 함께 사용 가능 (더 많은 기본 경로 포함)
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("=====securityFilterChain=====");

        // CSRF 비활성화 (REST API 개발중)
        http.csrf(AbstractHttpConfigurer::disable);
        // 특정 경로에 대한 접근 권한 설정
        http.authorizeHttpRequests(authorize -> authorize

                // 루트 경로 및 메인 페이지 (컨트롤러에서 "/" 또는 "/main"을 처리할 경우)
                .requestMatchers("/", "/main", "/test").permitAll()

                 // 회원가입, 이메일 인증, 로그인 등 API는 인증 없이 접근 허용
                .requestMatchers("/signup.html", "/login","/api/members/**", "/api/auth/**").permitAll()

                // H2 Console, Swagger UI, 업로드 파일 접근은 인증 없이 허용
                .requestMatchers("/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**", "/upload/**").permitAll()

                // 그 외 모든 요청은 인증 필요 (로그인 필요)
                .anyRequest().authenticated()
                )

                // 2. 폼 로그인 설정
                .formLogin(formLogin -> formLogin
                        .loginPage("/login") // 사용자 정의 로그인 페이지 URL (GET 요청)
                        .defaultSuccessUrl("/", true) // 로그인 성공 시 이동할 기본 페이지 (항상 이 URL로 이동)
                        .failureUrl("/login?error") // 로그인 실패 시 이동할 URL (error 파라미터 추가)
                        .usernameParameter("username") // 로그인 폼의 사용자명 필드 이름 (기본값)
                        .passwordParameter("password") // 로그인 폼의 비밀번호 필드 이름 (기본값)
                        .permitAll() // 로그인 페이지 자체는 모든 사용자에게 허용
                )

                // 3. 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/logout") // 로그아웃 처리 URL (POST 요청)
                        .logoutSuccessUrl("/login?logout") // 로그아웃 성공 시 로그인 페이지로 리다이렉트 (logout 파라미터 추가)
                        .invalidateHttpSession(true) // 세션 무효화
                        .deleteCookies("JSESSIONID", "remember-me") // JSESSIONID 및 remember-me 쿠키 삭제
                        .permitAll() // 로그아웃 관련 URL도 인증 없이 접근 가능해야 함
                )
                // 4. 자동 로그인 (Remember-Me) 설정
                .rememberMe(rememberMe -> rememberMe
                        .key("yourSecretKeyForRememberMe") // 고유하고 보안성 있는 키 설정 (예: UUID.randomUUID().toString())
                        .tokenValiditySeconds(60 * 60 * 24 * 7) // 토큰 유효 기간 (7일)
                        .userDetailsService(userDetailsService) // 사용자 정보를 로드할 MemberDetailsService 구현체 지정
                        .tokenRepository(persistentTokenRepository()) // JDBC 기반 토큰 저장소 사용
        );
        // H2 Console 프레임 허용 (개발 환경에서만 필요)
        http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

        // TODO: 추후 로그인, 로그아웃, 예외 처리 등을 추가

        return http.build();
    }

    // JDBC 기반 Remember-Me 토큰 저장소 설정 (PersistentTokenRepository)
    // Spring Security가 remember-me 토큰을 DB에 저장하고 관리하도록 합니다.
    @Bean
    public PersistentTokenRepository persistentTokenRepository() {
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        // 개발 단계에서 테이블이 없다면 자동 생성. 운영 시에는 DDL 스크립트로 직접 생성 권장.
        // jdbcTokenRepository.setCreateTableOnStartup(true); // <--- 이 줄은 최초 1회 실행 후 주석 처리하거나 삭제!
        return jdbcTokenRepository;
    }

}