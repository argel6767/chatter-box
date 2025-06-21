package com.chat_room_app.configs;

import com.chat_room_app.users.UserRepository;
import org.springframework.context.annotation.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


/**
 * Configures Beans that will be used in Authentication Tasks throughout application
 */
@Configuration
public class ApplicationConfiguration {

    private final UserRepository userRepository;

    public ApplicationConfiguration(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /*
     * desired behavior for when an email is not found among users
     */
    @Bean
    UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username).orElseThrow(
                () -> new UsernameNotFoundException("Email not attached to any user")
        );
    }

    /*
     * default password encoder used
     * BCrypt hard hashing
     */
    @Bean
    BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /*
     * returns an AuthenticationManager to handle user authentication, utilizing AuthenticationConfiguration
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /*
     * creates and returns a DaoAuthentication provider using the defined UserDetailsService (UserService)
     * as its UserDetailsService, as well as a BCryptPasswordEncoder as its password encoder
     */
    @Bean
    AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }
}
