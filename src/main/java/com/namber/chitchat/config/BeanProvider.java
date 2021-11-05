package com.namber.chitchat.config;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsResourceDetails;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class BeanProvider {

    @Value("${mongo.connectionUrl}")
    private String mongoUrl;

    @Bean
    ModelMapper modelMapper(){
        return new ModelMapper();
    }

    @Bean
    PasswordEncoder passwordEncoder(){
        Map<String, PasswordEncoder> encoderMap = new HashMap<>();
        encoderMap.put("noop", NoOpPasswordEncoder.getInstance());
        return new DelegatingPasswordEncoder("noop", encoderMap);
    }

    @Bean
    MongoClient mongoClient(){
        return MongoClients.create(mongoUrl);
    }


}
