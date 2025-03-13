package com.example.demo.config;

import com.example.demo.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtFilter jwtFilter;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtFilter jwtFilter) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtFilter = jwtFilter;
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(customizer -> customizer.disable())
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("signup", "/login").permitAll()
                        .anyRequest().authenticated())
                .httpBasic(Customizer.withDefaults())
//                .formLogin(Customizer.withDefaults()) // remove it else login will be intercepted by it not by our controller
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class) // before req going to this filter, we want it to go to JWT Filter, hence we will create it.
                .build();
        /**
         *
         *  just enabling .authorizeHttpRequests authenticates all request, but 403: UNAUTHORISED is thrown because till
         *  now we haven't set .httpBasic(Customizer.withDefaults()), this enables basic auth of POSTMAN
         *  still accessing from BROWSER is not possible, we need to set
         *  .formLogin(Customizer.withDefaults()) for that.(current popup in browser is not form its basic auth same as postman)
         *
         * */

    }

    /**
     * InMemoryUserDetailsManager class IMPLEMENTS UserDetailsManager Interface
     * UserDetailsManager interface EXTENDS UserDetailsService interface.
     * UserDetailsService class works with UsernamePasswordAuthenticationFilter to authenticate username and password.
     *InMemoryDetailsServiceManager needs an Object for UserDetails Interface.
     * 1. Either make an impl class of UserDetails Interface or 2.Use any class that implements it....like User class
     * */

//    @Bean
//    public UserDetailsService userDetailsService() {
//        UserDetails user = User
//                .withDefaultPasswordEncoder()
//                .username("shreyansh")
//                .password("abc")
//                .build();
//       return new InMemoryUserDetailsManager(user);
//    }

    /**
     * Since we now need to connect to DB, we need AuthenticationProvider which authorises the AuthenticationObject
     * AuthenticationProvider is an Interface, it has various impls, DaoAuthenticationProvider class impls AuthenticationProvider
     * This class helps connect to DB, and sets UserDetailsService(Interface).
     * Instead of using InMemoryUserDetailsManager as before, we will create our own implementation (CustomUserDetailsService) of UserDetailsService interface and set it.
     * */

    @Bean
    public AuthenticationProvider authenicationProvider(){
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
//        daoAuthenticationProvider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
        daoAuthenticationProvider.setPasswordEncoder(bCryptPasswordEncoder());
        daoAuthenticationProvider.setUserDetailsService(customUserDetailsService);
        return daoAuthenticationProvider;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // getting authenication manager which is underhood used by authenication provider, needed beacause we are using JWT now
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
