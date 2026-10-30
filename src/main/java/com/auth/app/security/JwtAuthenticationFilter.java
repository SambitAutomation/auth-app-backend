package com.auth.app.security;

import com.auth.app.helper.UserHelper;
import com.auth.app.repo.UserRepo;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

        private  final JwtService jwtService;
        private final UserRepo userRepo;

        private Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        logger.info("Auth Header : " + header);

        if(header != null && header.startsWith("Bearer ")){

            String token = header.substring(7);

            try{
                    Jws<Claims> parser = jwtService.parseToken(token);
                    Claims payload = parser.getPayload();

                    if(!jwtService.isAccessToken(token)){
                        filterChain.doFilter(request, response);
                        return;
                    }

                    String userId = payload.getSubject();
                    UUID userUuid = UserHelper.parseUUID(userId);

                    userRepo.findById(userUuid)
                            .ifPresent(user -> {

                                if(user.isEnable()){
                                    List<GrantedAuthority> authorities = user.getRoles() == null ? List.of() :
                                            user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getName()))
                                                    .collect(Collectors.toList());

                                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                            user.getEmail(),
                                            null,
                                            authorities
                                    );

                                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                                    if(SecurityContextHolder.getContext().getAuthentication() == null)
                                        SecurityContextHolder.getContext().setAuthentication(authentication);
                                }
                            });


            }catch (ExpiredJwtException e){
                request.setAttribute("error", "Expired Token");
            }catch (Exception e){
                request.setAttribute("error", "Invalid Token");
            }

            filterChain.doFilter(request, response);
        } else {
            filterChain.doFilter(request, response);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getRequestURI().startsWith("/api/v1/auth/login");
    }
}
