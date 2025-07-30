package webproject_2team.lunch_matching.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User; // Spring Security의 User 클래스
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import webproject_2team.lunch_matching.domain.Member;
import webproject_2team.lunch_matching.domain.MemberRole; // MemberRole Enum 임포트
import webproject_2team.lunch_matching.repository.MemberRepository;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class MemberDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Attempting to load user by username: {}", username);

        // 1. DB에서 username으로 Member 엔티티 조회
        Member member = memberRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found: {}", username);
                    return new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username);
                });

        log.info("User found: {}", member.getUsername());

        // 2. Member 엔티티의 역할을 Spring Security의 GrantedAuthority로 변환
        // Member 엔티티의 roles 필드(Set<MemberRole> Enum)를 활용합니다.
        Collection<GrantedAuthority> authorities = member.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name())) // Enum.name()으로 문자열 변환
                .collect(Collectors.toList());

        log.info("User roles: {}", authorities);

        // 3. Spring Security의 UserDetails 구현체인 User 객체 생성 및 반환
        return new User(
                member.getUsername(),   // 인증에 사용될 사용자명 (아이디)
                member.getPassword(),   // 암호화된 비밀번호 (Spring Security가 매칭해줌)
                authorities             // 사용자에게 부여된 권한 목록
        );
    }
}

