package com.ecommerce.app.security;

import com.ecommerce.app.entity.User;
import com.ecommerce.app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Custom implementation of UserDetailsService for Spring Security.
 * Loads user information from database and converts it to Spring's UserDetails format.
 * Used for authentication and authorization.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Loads user by email (username in our case is email).
     * Spring Security calls this method to get user details during authentication.
     *
     * @param email the email/username of the user to load
     * @return UserDetails object containing user information and authorities
     * @throws UsernameNotFoundException if user with given email is not found
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find user by email
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Convert user's role to GrantedAuthority (Spring Security format)
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().toString()));

        // Return Spring Security UserDetails object
        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),                    // username (we use email as unique identifier)
            user.getPassword(),                 // password (already encoded in database)
            !user.getAccountLocked(),           // enabled (opposite of locked - if locked, disabled)
            true,                               // accountNonExpired
            true,                               // credentialsNonExpired
            !user.getAccountLocked(),           // accountNonLocked (opposite of locked)
            authorities                         // authorities/roles
        );
    }
}
