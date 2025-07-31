package webproject_2team.lunch_matching.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class RootConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
            //"나는 PasswordEncoder라는 역할(인터페이스)을 수행할 객체로 BCryptPasswordEncoder라는 구체적인 구현체를 사용할 것
        // Spring이 애플리케이션 시작 시점에 BCryptPasswordEncoder 객체를 생성하여
            // PasswordEncoder 타입의 빈으로 등록하고, 이 빈을 다른 서비스나 컨트롤러에서 주입받아 비밀번호 암호화 로직에 활용할 수 있도록 하기 위함
    }
}
