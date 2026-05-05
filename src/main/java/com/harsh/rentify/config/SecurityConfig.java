package com.harsh.rentify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final Environment environment;

    public SecurityConfig(Environment environment) {
        this.environment = environment;
    }

    private boolean devProfileActive() {
        return environment.acceptsProfiles(Profiles.of("dev"));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/", "/login", "/register", "/error", "/error/**", "/css/**", "/js/**", "/images/**", "/uploads/**").permitAll();
                    if (devProfileActive()) {
                        auth.requestMatchers("/h2-console/**").permitAll();
                    }
                    auth.requestMatchers(HttpMethod.GET, "/rooms/**", "/profiles/**").permitAll();
                    auth.requestMatchers("/admin/**").hasRole("ADMIN");
                    auth.requestMatchers("/landlord/**", "/add-property").hasRole("LANDLORD");
                    auth.requestMatchers("/tenant/**").hasRole("TENANT");
                    auth.requestMatchers("/account/**", "/dashboard").authenticated();
                    auth.anyRequest().authenticated();
                }
        );

        http.formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .defaultSuccessUrl("/dashboard?loginSuccess=true", true)
                .failureUrl("/login?error=true")
                .permitAll()
        );

        http.logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/login?logout=true")
        );

        http.exceptionHandling(exception -> exception.accessDeniedPage("/error/403"));
        http.csrf(csrf -> {
            if (devProfileActive()) {
                csrf.ignoringRequestMatchers(new AntPathRequestMatcher("/h2-console/**"));
            }
        });
        http.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }
}
