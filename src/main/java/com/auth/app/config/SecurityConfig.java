package com.auth.app.config;

import com.auth.app.dto.ApiError;
import com.auth.app.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private  JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http){

     return    http.csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                        .sessionManagement(sm ->
                                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(authReq ->
                authReq.requestMatchers("/api/v1/auth/*").permitAll()
                        .anyRequest().authenticated()
                ).
        exceptionHandling(ex -> ex.authenticationEntryPoint(((request, response, e) -> {
                e.printStackTrace();
                response.setStatus(401);
                response.setContentType("application/json");
                String message = e.getMessage();

                String error = (String) request.getAttribute("error");
                if(error != null){
                    message = error;
                }
//                Map<String, Object>  errorMap = Map.of("message", message, "status", String.valueOf(401), "statusCode", Integer.toString(401));
               var apiError = ApiError.of(HttpStatus.UNAUTHORIZED.value(), "Unauthorized", message, request.getRequestURI());
                var objectMapper = new ObjectMapper();
                response.getWriter().write(objectMapper.writeValueAsString(apiError));

    })))
             .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public UserDetailsService  users(){
//        User.UserBuilder userBuilder = User.withDefaultPasswordEncoder();
//        UserDetails  user1 = userBuilder.username("sambit").password("1234").roles("ADMIN").build();
//
//        return new InMemoryUserDetailsManager(user1);
//    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration){
        return authenticationConfiguration.getAuthenticationManager();
    }
}
