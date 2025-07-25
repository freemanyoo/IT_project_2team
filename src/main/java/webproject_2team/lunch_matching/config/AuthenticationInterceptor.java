package webproject_2team.lunch_matching.config;

import com.example.kmj.entity.User;
import com.example.kmj.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.security.Principal;
import java.time.LocalDateTime;

/**
 * 사용자 인증 및 정지 상태를 확인하는 인터셉터 클래스입니다.
 * 요청이 컨트롤러에 도달하기 전에 사용자의 접근 권한을 확인합니다.
 */
@Component
@RequiredArgsConstructor
public class AuthenticationInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    /**
     * 요청 처리 전에 실행되는 메서드입니다.
     * 로그인한 사용자가 정지 상태인지 확인하고, 정지 상태라면 접근 거부 페이지로 리다이렉트합니다.
     * @param request 현재 HTTP 요청
     * @param response 현재 HTTP 응답
     * @param handler 실행될 핸들러 (컨트롤러 메서드)
     * @return 요청을 계속 처리할지 (true) 또는 중단할지 (false) 여부
     * @throws Exception 예외 발생 시
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElse(null);
            if (user != null && user.getSuspendedUntil() != null && user.getSuspendedUntil().isAfter(LocalDateTime.now())) {
                response.sendRedirect("/access-denied");
                return false;
            }
        }
        return true;
    }
}
