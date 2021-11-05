package com.namber.chitchat.service;

import com.namber.chitchat.dao.AppUserRepo;
import com.namber.chitchat.model.AppUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppUserDetailsService implements UserDetailsService {
    @Autowired
    private AppUserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = userRepo.getUser(username);
        if (user != null){
            List<GrantedAuthority> authorityList = user.getAuthorities().stream().map(auth -> new SimpleGrantedAuthority(auth)).collect(Collectors.toList());
            return new User(username, user.getPassword(), authorityList);
        }

        return null;
    }
}
