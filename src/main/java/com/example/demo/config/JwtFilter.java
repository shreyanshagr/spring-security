package com.example.demo.config;

import com.example.demo.service.CustomUserDetailsService;
import com.example.demo.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    /**
     * Token in the request's Header from client side that out filter intercepts looks like :
     * Bearer eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJNaWEiLCJpYXQiOjE3NDE3MDc0NjAsImV4cCI6MTc0MTcwODY2MH0.Ot0W-QsAkn9FUXd3KZExnAa7yTDyBNdHPIr4P3_KR10
     * We need to get substring for the token part(remove Bearer<whitespace>)
     * Also we can get Username from the payload part of the token.
     */

    @Autowired
    private JWTService jwtService;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if(authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            username = jwtService.extractUsername(token);
        }


        /**
         * Before UsernamePasswordAuthenticationFilter, we want JWTFilter to intercept the request first.
         * 1. Here we will authenticate the token.
         * 2. If successfully authenicated, we create an Authentication object for UsernamePasswordAuthenticationFilter
         * Here we extract the token, username and check if the user is not already authenicated.
         * How to check if the request is already authenticated??
         * Ans: Check SecurityContextHolder if its authenticated.. i.e.
         * SecurityContextHolder.getContext().getAuthentication() != null
         * */

        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            /**
             * Here we check 2 things.
             * a. Validation of token.
             * b. If the username exists in DB or not.(How? Using DaoAuthenticationProvider as it deals with DB.)
             * We need to check the username existence because any valid JWT Token for any deleted user can also
             * be used to access resources.
             * */
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
            logger.info(userDetails.getUsername());
            logger.info(userDetails.getPassword());
            if (jwtService.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
