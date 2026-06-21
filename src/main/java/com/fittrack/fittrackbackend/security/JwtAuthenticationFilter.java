package com.fittrack.fittrackbackend.security;

import com.fittrack.fittrackbackend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private  final JwtService jwtService;
    @Override
   protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain)
    throws ServletException, IOException {
        final String authHeader= request.getHeader("Authorization");
        if(authHeader==null || !authHeader.startsWith("Bearer ")){
            filterChain.doFilter(request,response);
            return;
        }
        final String jwtToken=authHeader.substring(7);
        final String userEmail= jwtService.extractEmail(jwtToken);
        if(userEmail!=null && SecurityContextHolder.getContext().getAuthentication()==null){
            if(jwtService.isTokenValid(jwtToken)){
                UsernamePasswordAuthenticationToken authentication=new UsernamePasswordAuthenticationToken(userEmail,null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        filterChain.doFilter(request,response);
        System.out.println(SecurityContextHolder.getContext().getAuthentication());
    }
}
