package com.ensam.bloodlink.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // chaque role a ses propres url (R12)
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/inscription", "/css/**", "/error").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN_SYSTEME")
                        .requestMatchers("/donneur/**").hasRole("DONNEUR")
                        .requestMatchers("/medecin/**").hasRole("MEDECIN")
                        .requestMatchers("/stock/**").hasRole("GESTIONNAIRE_STOCK")
                        .requestMatchers("/hopital/**").hasRole("ADMIN_HOPITAL")
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/accueil", true)
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll());
        return http.build();
    }
}