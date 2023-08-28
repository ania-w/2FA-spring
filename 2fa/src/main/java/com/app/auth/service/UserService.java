package com.app.auth.service;

import com.app.auth.model.JwtToken;
import com.app.auth.model.Roles;
import com.app.auth.model.User;
import com.app.auth.model.UserDetailsImpl;
import com.app.auth.model.communication.response.CreateAccountResponse;
import com.app.auth.model.communication.response.User2faData;
import com.app.auth.repository.UserRepository;
import com.app.auth.service.util.JwtUtils;
import com.app.auth.service.util.QrCodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;

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
    public void setIsActive(String username, Boolean isActive){
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User " + username + "not found in the system."));
        user.setIsActive(isActive);
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

    public void update(User user) {
        userRepository.save(user);
    }

    public byte[] generateNewConfigData(String username) throws Exception {
            User2faData user2faData = new User2faData(username);
            byte[] qrCode = QrCodeUtil.generateQR(user2faData.toString());
            String rsaPrivate = Base64.getEncoder().encodeToString(user2faData.getKeyPair().getPrivate().getEncoded());

            User user = findUserByUsername(username);
            user.setDeviceId(user2faData.getDeviceId());
            user.setPassword(rsaPrivate);

            update(user);

            return qrCode;
    }
}
