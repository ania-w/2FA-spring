package com.ania.auth.service;

import com.ania.auth.model.TwoFactorMethod;
import com.ania.auth.model.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

@AllArgsConstructor
public class UserDetailsImpl implements UserDetails {

    private Long id;

    private String username;

    @JsonIgnore
    private String password;

    private String email;

    private String secret;

    private String twoFactorMethod;

    private Boolean twoFactorEnabled;

    public static UserDetailsImpl build(User user) {

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getSecret(),
                user.getTwoFactorMethod(),
                user.getTwoFactorEnabled());
    }


    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getEmail() {
        return email;
    }

    public String getSecret() {
        return secret;
    }

    public String getTwoFactorMethod() {
        return twoFactorMethod;
    }

    public Boolean getTwoFactorEnabled() {
        return twoFactorEnabled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        UserDetailsImpl user = (UserDetailsImpl) o;
        return Objects.equals(id, user.id);
    }
}