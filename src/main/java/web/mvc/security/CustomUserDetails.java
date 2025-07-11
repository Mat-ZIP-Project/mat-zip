package web.mvc.security;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import web.mvc.domain.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
@Slf4j
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {this.user = user;}

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        log.info("getAuthorities 호출...");
        List<GrantedAuthority> authorities = new ArrayList<>();
        String role = user.getRole(); // DB에서 "ROLE_OWNER" 또는 "OWNER" 형태

        // DB에 "OWNER"로 저장된 경우 "ROLE_" prefix 추가
        if (role != null && !role.startsWith("ROLE_")) {
            role = "ROLE_" + role;
        }
        authorities.add(new SimpleGrantedAuthority(role));
        log.info("사용자 권한 설정: {}", role);

        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUserId();
    }

    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return !"탈퇴".equals(user.getUserStatus()); }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return "활성".equals(user.getUserStatus()); }
}
