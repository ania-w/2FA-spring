package com.ania.auth.config;

import com.ania.auth.service.AuthTokenFilter;
import com.ania.auth.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

        @Autowired
        UserDetailsServiceImpl userDetailsService;

        @Autowired
        AuthTokenFilter tokenFilter;

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
            DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();

            authProvider.setUserDetailsService(userDetailsService);
            authProvider.setPasswordEncoder(passwordEncoder());

            return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
            return authConfig.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
            return new BCryptPasswordEncoder();
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

            http
                    .requiresChannel(channel ->
                            channel.anyRequest().requiresSecure())
                    .securityMatcher("/api/content/**")
                    .authorizeHttpRequests((requests) -> requests.requestMatchers("/api/auth/**").permitAll()
                                            .anyRequest().authenticated())
                    .cors().and().csrf().disable()
                    .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                            .addFilterBefore(tokenFilter,UsernamePasswordAuthenticationFilter.class)
                    .headers().frameOptions().sameOrigin().and()
                    .authenticationProvider(authenticationProvider())
                    .exceptionHandling().authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/api/auth/login"));


            return http.build();
        }

}