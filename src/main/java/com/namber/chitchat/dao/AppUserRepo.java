package com.namber.chitchat.dao;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.namber.chitchat.model.AppUser;
import com.namber.chitchat.model.People;
import com.namber.chitchat.model.UserPreference;
import org.bson.Document;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class AppUserRepo {
    private String USERNAME = "username";


    @Value("${mongo.appDB}")
    private String appDB;

    @Value("${mongo.userCollection}")
    private String userCollection;

    @Value("${mongo.userPrefCollection}")
    private String userPrefCollection;


    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private ModelMapper mapper;

    public AppUser getUser(String username) {
        MongoCollection<Document> userCollection = mongoClient.getDatabase(appDB).getCollection(this.userCollection);
        return mapper.map(userCollection.find(new Document(USERNAME, username)).first(), AppUser.class);
    }

    public UserPreference getUserPref(String username) {
        MongoCollection<Document> userCollection = mongoClient.getDatabase(appDB).getCollection(this.userPrefCollection);
        return mapper.map(userCollection.find(new Document(USERNAME, username)).first(), UserPreference.class);
    }
}
