package com.poly.config;

import com.google.gson.Gson;
import com.poly.common.UserType;
import com.poly.service.UserServiceDetail;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import sendinblue.ApiClient;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@RequiredArgsConstructor
public class AppConfig {

    private final CustomizeRequestFilter requestFilter;
    private final UserServiceDetail userServiceDetail;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(request -> request
                        // Public endpoints
                        .requestMatchers(
                                "/auth/**", "/auth/google",
                                "/user/add",
                                "/user/confirm-email",
                                "/actuator/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/webjars/**",
                                "/swagger-resources/**",
                                "/favicon.ico"
                        ).permitAll()

                        // Product endpoints
                        .requestMatchers(HttpMethod.GET, "/product/**").permitAll()
                        .requestMatchers("/product/**").hasAuthority(UserType.ADMIN.name())

                        // Category endpoints
                        .requestMatchers(HttpMethod.GET, "/category/**").permitAll()
                        .requestMatchers("/category/**").hasAuthority(UserType.ADMIN.name())

                        // User endpoints
                        .requestMatchers("/user/profile", "/user/upd", "/user/change-pwd").authenticated()
                        .requestMatchers("/user/list", "/user/{userId}","/user/del/{userId}").hasAuthority(UserType.ADMIN.name())

                        // Address endpoints
                        .requestMatchers("/address/**").authenticated()

                        // Cart endpoints
                        .requestMatchers("/cart/**").authenticated()

                        // Order endpoints
                        .requestMatchers("/order/**").authenticated()
                        .requestMatchers("/order/list").hasAuthority(UserType.ADMIN.name())

                        // Any other request requires authentication
                        .anyRequest().authenticated()
                )
                .sessionManagement(manager -> manager.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(requestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public WebSecurityCustomizer ignoreResources() {
        return webSecurity -> webSecurity
                .ignoring()
                .requestMatchers(
                        "/actuator/**",
                        "/v3/**",
                        "/webjars/**",
                        "/swagger-ui*/*swagger-initializer.js",
                        "/swagger-ui*/**",
                        "/favicon.ico"
                );
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userServiceDetail.userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(frontendUrl, "http://localhost:5173")
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("Authorization")
                        .allowCredentials(true)
                        .maxAge(3600);
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ApiClient brevoApiClient(@Value("${spring.brevo.apiKey}") String apiKey) {
        ApiClient apiClient = new ApiClient();
        apiClient.setApiKey(apiKey);
        return apiClient;
    }

    @Bean
    public Gson gson() {
        return new Gson();
    }
}