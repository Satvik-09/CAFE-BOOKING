package com.cafe.booking.Filter;

import com.cafe.booking.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFilter extends OncePerRequestFilter {
//used to access the method inside jwtutil
    private  final JwtUtil jwtutil;

    public JwtFilter(JwtUtil jwtutil){
        this.jwtutil = jwtutil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
         throws ServletException,IOException {
         String authHeader = request.getHeader("Authorization");

         if(authHeader != null && authHeader.startsWith("Bearer")){
             String token = authHeader.substring(7);

             if(jwtutil.isTokenValid(token)){

                 String username = jwtutil.extractusername(token);
                 String role = jwtutil.extractRole(token);

                 UsernamePasswordAuthenticationToken authentication =
                         new UsernamePasswordAuthenticationToken(
                                 username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));

                  // it stores the authentication into the securitycontext container
                 SecurityContextHolder.getContext().setAuthentication(authentication);
             }

         }
         filterChain.doFilter(request,response);
        }






}
