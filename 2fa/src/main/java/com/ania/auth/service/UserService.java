package com.ania.auth.service;

import com.ania.auth.model.User;
import com.ania.auth.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService implements UserDetailsService{
    @Autowired
    UserRepository userRepository;

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return UserDetailsImpl.build(findUserByUsername(username));
    }

    @Transactional
    public User findUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + "not found in the system."));
    }

    @Transactional
    public void setTwoFactorMethodEnabled(String username, Boolean twoFactorMethodEnabled){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + "not found in the system."));
        user.setTwoFactorEnabled(twoFactorMethodEnabled);
        userRepository.save(user);
    }

    @Transactional
    public void printAllUsers(){
        userRepository.findAll().forEach(System.out::println);
    }

    public void save(User user) {
        if(userRepository.findByUsername(user.getUsername()).isPresent()){
            throw new RuntimeException("Username already taken");
        }
        userRepository.save(user);
    }
}
