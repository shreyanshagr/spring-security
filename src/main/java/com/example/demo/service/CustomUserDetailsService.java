package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.payload.UserPrincipal;
import com.example.demo.repository.UserRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepo userRepo;

    public CustomUserDetailsService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }


    @Override
    // this function will loaduser from DB
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepo.findByUsername(username);
        if (user == null) {
            logger.info("User does not exists with the username -> {}", username);
            throw new UsernameNotFoundException("User does not exists with the username : " + username);
        }
        // here we need to return UserDetails object, but its an interface, we need to create its impl.
        return new UserPrincipal(user);

    }
}
/**
 * UserDetailsService is an interface that has abstract method -> public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
 * UserDetailsService Interface also has an internal implementation class -> User.class
 * We used that User class's builder pattern to build and object which
 * */