package app.quizmaster.config;

import app.quizmaster.repo.UserRepo;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean UserDetailsService userDetailsService(UserRepo users) {
        return email -> users.findByEmailIgnoreCase(email).map(AppUser::new)
                .orElseThrow(() -> new UsernameNotFoundException("No account for " + email));
    }

    /** Send each role straight to its own desk after sign-in. */
    @Bean AuthenticationSuccessHandler roleAwareSuccessHandler() {
        return (request, response, authentication) -> {
            String target = "/";
            for (GrantedAuthority a : authentication.getAuthorities()) {
                switch (a.getAuthority()) {
                    case "ROLE_ADMIN" -> target = "/admin/dashboard";
                    case "ROLE_TEACHER" -> target = "/teacher";
                    case "ROLE_STUDENT" -> target = "/student";
                    default -> { }
                }
            }
            response.sendRedirect(request.getContextPath() + target);
        };
    }

    @Bean SecurityFilterChain filterChain(HttpSecurity http, AuthenticationSuccessHandler roleAwareSuccessHandler) throws Exception {
        http
            .authorizeHttpRequests(a -> a
                .requestMatchers("/", "/login", "/register", "/register/**", "/css/**", "/js/**", "/images/**", "/error").permitAll()
                .requestMatchers("/teacher/**").hasRole("TEACHER")
                .requestMatchers("/student/**").hasRole("STUDENT")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(f -> f.loginPage("/login").usernameParameter("email").successHandler(roleAwareSuccessHandler).permitAll())
            .logout(l -> l.logoutUrl("/logout").logoutSuccessUrl("/login?out").permitAll())
            .headers(h -> h.frameOptions(fo -> fo.sameOrigin()));
        return http.build();
    }
}
