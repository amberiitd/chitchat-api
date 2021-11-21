package com.namber.chitchat.dao;

import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.UpdateOptions;
import com.namber.chitchat.model.AppUser;
import com.namber.chitchat.model.People;
import com.namber.chitchat.model.PublicUserPreference;
import com.namber.chitchat.model.UserPreference;
import org.bson.Document;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
public class AppUserRepo {
    private static String USERNAME = "username";
    private static String PUBLIC_USERNAME = "publicUsername";

    @Value("${mongo.appDB}")
    private String appDB;

    @Value("${mongo.userCollection}")
    private String userCollection;

    @Value("${mongo.userPrefCollection}")
    private String userPrefCollection;

    @Value("${mongo.publicUserPrefCollection}")
    private String publicUserPrefCollection;


    @Autowired
    private MongoClient mongoClient;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    Gson gson;

    public AppUser getUser(String username) {
        MongoCollection<Document> userCollection = mongoClient.getDatabase(appDB).getCollection(this.userCollection);
        return mapper.map(userCollection.find(new Document(USERNAME, username)).first(), AppUser.class);
    }

    public UserPreference getUserPref(String username) {
        MongoCollection<Document> userPrefCollection = mongoClient.getDatabase(appDB).getCollection(this.userPrefCollection);
        List<Document> orList= Arrays.asList(
                new Document(PUBLIC_USERNAME, username),
                new Document(USERNAME, username)
        );

        Document doc = userPrefCollection.find(new Document("$or", orList)).first();
        if(doc != null) {
            return mapper.map(doc, UserPreference.class);
        }

        return null;
    }

    public UserPreference getUserPrefByPublicUsername(String publicUsername) {
        MongoCollection<Document> userPrefCollection = mongoClient.getDatabase(appDB).getCollection(this.userPrefCollection);
        Document doc = userPrefCollection.find(new Document(PUBLIC_USERNAME, publicUsername)).first();
        if(doc != null) {
            return mapper.map(doc, UserPreference.class);
        }

        return null;
    }

    public void incUserPrefUnseenCount(String to, String from, int add){
        updateUserPrefConv("unseenCount", to, from, "$inc", add);
    }

    public void incUserPrefNotViewedCount(String to, String from, int add) {
        updateUserPrefConv("notViewedCount", from, to, "$inc", add);
    }

    public void unsetUserPrefUnseenCount(String to, String from){
        updateUserPrefConv("unseenCount", to, from, "$set", 0);
    }

    public void unsetUserPrefNotViewedCount(String to, String from) {
        updateUserPrefConv("notViewedCount", from, to, "$set", 0);
    }

    public void updateUserPrefPinned(String user, String people, long val) {
        updateUserPrefConv("pinned", user, people, "$set", val);
    }


    private void updateUserPrefConv(String att, String user, String other, String op, long val){
        MongoCollection<Document> userPrefCollection = mongoClient.getDatabase(appDB).getCollection(this.userPrefCollection);
        userPrefCollection.updateOne(
                new Document(PUBLIC_USERNAME, user),
                new Document(op, new Document("conversations.$[element]."+att, val)),
                new UpdateOptions().arrayFilters(Arrays.asList(new Document("element."+ PUBLIC_USERNAME, other)))
        );
    }

    public void pushToUserPrefConv(String username, People conv){
        MongoCollection<Document> userPrefCollection = mongoClient.getDatabase(appDB).getCollection(this.userPrefCollection);
        userPrefCollection.updateOne(
                new Document(PUBLIC_USERNAME, username),
                new Document("$push", new Document("conversations", Document.parse(gson.toJson(conv))))
        );
    }

    public PublicUserPreference getPublicUserPref(String publicUsername) {
        MongoCollection<Document> userPrefCollection = mongoClient.getDatabase(appDB).getCollection(this.publicUserPrefCollection);
        return mapper.map(userPrefCollection.find(new Document(PUBLIC_USERNAME, publicUsername)).first(), PublicUserPreference.class);
    }

    public PublicUserPreference findPublicUserPref(String publicUsername) {
        MongoCollection<Document> userPrefCollection = mongoClient.getDatabase(appDB).getCollection(this.publicUserPrefCollection);
        try{
           return mapper.map(userPrefCollection.find(new Document(PUBLIC_USERNAME, publicUsername)).first(), PublicUserPreference.class);
        }catch (Exception e){
            return null;
        }
    }

    public void pushToUserPrefContact(String username, People people) {
        MongoCollection<Document> userPrefCollection = mongoClient.getDatabase(appDB).getCollection(this.userPrefCollection);
        userPrefCollection.updateOne(
                new Document(PUBLIC_USERNAME, username),
                new Document("$push", new Document("contacts", Document.parse(gson.toJson(people))))
        );
    }

    public void saveUser(AppUser user) {
        mongoClient.getDatabase(appDB).getCollection(this.userCollection)
                .insertOne(Document.parse(gson.toJson(user)));
    }

    public void saveUserPrefence(UserPreference userPreference) {
        mongoClient.getDatabase(appDB).getCollection(this.userPrefCollection)
                .insertOne(Document.parse(gson.toJson(userPreference)));
    }

    public void savePublicUserPrefence(PublicUserPreference publicUserPreference) {
        mongoClient.getDatabase(appDB).getCollection(this.publicUserPrefCollection)
                .insertOne(Document.parse(gson.toJson(publicUserPreference)));
    }

    public void deleteChat(String username, String publicUsername) {
        mongoClient.getDatabase(appDB).getCollection(this.userPrefCollection)
            .updateOne(
                new Document(USERNAME, username),
                new Document("$pull", new Document("conversations", new Document(PUBLIC_USERNAME, publicUsername)))
            );
    }

    public long convExists(String user, String from){
        return mongoClient.getDatabase(appDB).getCollection(this.userPrefCollection).count(
                new Document(PUBLIC_USERNAME, user).append("conversations", new Document("$elemMatch", new Document(PUBLIC_USERNAME, from)))
        );
    }

    public long contactExists(String user, String from){
        return mongoClient.getDatabase(appDB).getCollection(this.userPrefCollection).count(
                new Document(PUBLIC_USERNAME, user).append("contacts", new Document("$elemMatch", new Document(PUBLIC_USERNAME, from)))
        );
    }

    public void deleteContact(String username, String publicUsername) {
        mongoClient.getDatabase(appDB).getCollection(this.userPrefCollection)
                .updateOne(
                        new Document(USERNAME, username),
                        new Document("$pull", new Document("contacts", new Document(PUBLIC_USERNAME, publicUsername)))
                );
    }
}
