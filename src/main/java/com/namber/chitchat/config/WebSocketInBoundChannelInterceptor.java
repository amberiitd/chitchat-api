package com.namber.chitchat.config;

import com.namber.chitchat.service.UserPrefService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessageType;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.web.client.RestTemplate;
import sun.net.www.http.HttpClient;

import javax.naming.AuthenticationException;

@Configuration
@Slf4j
public class WebSocketInBoundChannelInterceptor implements ChannelInterceptor {
    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserPrefService userPrefService;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor headerAccessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (SimpMessageType.CONNECT.equals(headerAccessor.getMessageType())){
            String username = headerAccessor.getLogin();
            String passcode = headerAccessor.getPasscode();
            try {
                headerAccessor.setUser(authenticate(username, passcode));
            }catch (Exception e){
                log.warn("User is not registered!");
            }
        }
        return message;
    }

    private Authentication authenticate(String username, String passcode) throws AuthenticationException{
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (userDetails!= null && encoder.matches(passcode, userDetails.getPassword())){
            return new UsernamePasswordAuthenticationToken( userPrefService.getPublicUsername(username), null, userDetails.getAuthorities());
        }else{
            throw new AuthenticationException("Incorrect Password");
        }
    }
}
