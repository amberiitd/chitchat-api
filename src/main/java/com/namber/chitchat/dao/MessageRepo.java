package com.namber.chitchat.dao;

import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.namber.chitchat.model.Message;
import com.namber.chitchat.model.MessageQuery;
import org.bson.Document;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.regex.Pattern;

@Repository
public class MessageRepo {
    @Autowired
    private MongoClient mongoClient;

    @Value("${mongo.appDB}")
    private String appDB;

    private static String MSG_COL_PRE = "msg_";
    private static String FROM ="from";
    private static String TO ="to";

    @Autowired
    ModelMapper mapper;

    private Gson gson = new Gson();

    public void save(String username, Message msg) {
        mongoClient.getDatabase(appDB).getCollection(MSG_COL_PRE+ username).insertOne(Document.parse(gson.toJson(msg)));
    }

    public List<Message> fetchMessages(MessageQuery query){
        List<Message> msgs = new ArrayList<>();

        String collection = MSG_COL_PRE+ query.getUsername();
        Document dbQuery = buildQuery(query);

        Iterator<Document> iter = mongoClient.getDatabase(appDB).getCollection(collection).find(dbQuery)
                .sort(new Document("timestamp", query.getSort()))
                .limit(query.getCount())
                .iterator();
        while (iter.hasNext()){
            msgs.add(mapper.map(iter.next(), Message.class));
        }
        Collections.reverse(msgs);
        return msgs;
    }

    private Document buildQuery(MessageQuery query) {
        Document queryDoc = new Document();
        List<Document> orList= Arrays.asList(
                new Document(FROM, query.getFrom()),
                new Document(TO, query.getFrom())
        );
        queryDoc.append("$or", orList);

        if (query.getSearchText() != null){
            queryDoc.append("text", new Document("$regex", ".*" + Pattern.quote(query.getSearchText()) + ".*").append("$options", 'i'));
        }

        if (query.getStartTime() > 0L){
            queryDoc.append("timestamp", new Document("$gte", query.getStartTime()));
        }

        if (query.getEndTime() > 0L){
            queryDoc.append("timestamp", new Document("$lt", query.getEndTime()));
        }

        if(query.getTimestamp() > 0){
            queryDoc.append("timestamp", query.getTimestamp());
        }

        return queryDoc;
    }

    public void setViewed(String to, String from, long endTime) {
        // notif sender
        String collection = MSG_COL_PRE+ to;
        mongoClient.getDatabase(appDB).getCollection(collection).updateMany(
                new Document("timestamp", new Document("$lte", endTime)).append("notViewed", true),
                new Document("$set", new Document("notViewed", false))
            );
        // notif reciever
        collection = MSG_COL_PRE+ from;
        mongoClient.getDatabase(appDB).getCollection(collection).updateMany(
                new Document("timestamp", new Document("$lte", endTime)).append("notViewed", true),
                new Document("$set", new Document("notViewed", false))
        );
    }
}
