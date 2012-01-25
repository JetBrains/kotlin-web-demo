package org.jetbrains.demo.ukhorskaya.database;

import com.mongodb.*;
import org.jetbrains.demo.ukhorskaya.ErrorWriter;
import org.jetbrains.demo.ukhorskaya.ErrorWriterOnServer;
import org.jetbrains.demo.ukhorskaya.session.SessionInfo;
import org.jetbrains.demo.ukhorskaya.session.UserInfo;
import org.json.JSONArray;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 1/25/12
 * Time: 10:10 AM
 */

public class MongoDBConnector {
    private final DB database;

    private static final MongoDBConnector connector = new MongoDBConnector();

    private MongoDBConnector() {
        Mongo m = null;
        try {
            m = new Mongo("localhost", 27017);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            database = null;
            return;
        }

        database = m.getDB("kotlinDatabase");
    }

    public static MongoDBConnector getInstance() {
        return connector;
    }


    public boolean addNewUser(UserInfo info) {
        if (!findUser(info)) {
            DBCollection userIdUserInfo = database.getCollection("userIdUserInfo");
            BasicDBObject user = new BasicDBObject();
            user.put("id", info.getId());
            user.put("type", info.getType());
            user.put("name", info.getName());
            userIdUserInfo.insert(user);
            return true;
        }
        return false;
    }

    public boolean findUser(UserInfo info) {
        DBCollection coll = database.getCollection("userIdUserInfo");
        DBCursor cur = coll.find();
        while (cur.hasNext()) {
            DBObject object = cur.next();
            if (object.get("id").equals(info.getId()) && object.get("type").equals(info.getType())) {
                return true;
            }
        }

        return false;

    }

    public String addProgramInfo(UserInfo userInfo, String programText) {
        if (findUser(userInfo)) {
            DBCollection userIdProgramId = database.getCollection("userIdProgramId");
            BasicDBObject field = new BasicDBObject();
            field.put("id", userInfo.getId());
            field.put("type", userInfo.getType());
            field.put("programId", userInfo.getId() + programText.hashCode());
            userIdProgramId.insert(field);

            DBCollection programIdProgramInfo = database.getCollection("programIdProgramInfo");
            BasicDBObject program = new BasicDBObject();
            program.put("id", userInfo.getId() + programText.hashCode());
            program.put("name", "Program" + userInfo.getId() + programText.hashCode());
            program.put("text", programText);
            program.put("link", "");
            programIdProgramInfo.insert(program);
            return "Program" + userInfo.getId() + programText.hashCode();
        } else {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                    SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), "Cannot find user at userIdUserInfo table",
                    userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName()));
        }
        return "";
    }

    public String getProgramText(String programId) {
        DBCollection programIdProgramInfo = database.getCollection("programIdProgramInfo");
        BasicDBObject program = new BasicDBObject();
        program.put("id", programId);

        DBObject result = programIdProgramInfo.findOne(program);
        return (String) result.get("text");
    }

    public String getListOfProgramsForUser(UserInfo info) {
        BasicDBObject user = new BasicDBObject();
        user.put("name", info.getName());
        user.put("id", info.getId());
        user.put("type", info.getType());

        DBCollection coll = database.getCollection("userIdProgramId");
        DBCursor cur = coll.find();
        JSONArray result = new JSONArray();
        while (cur.hasNext()) {
            DBObject object = cur.next();
            if (object.get("id").equals(info.getId())) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("name", (String) object.get("programId"));
                map.put("id", (String) object.get("programId"));
                result.put(map);
            }
        }

        return result.toString();
    }

    public boolean addUserInfo(UserInfo info, String fieldName, String fieldValue) {
        if (findUser(info)) {
            DBCollection actions = database.getCollection("actions");
            BasicDBObject user = new BasicDBObject();
            user.put("id", info.getId());
            user.put("type", info.getType());
            DBCursor cur = actions.find(user);

            if (cur.hasNext()) {
                user.put(fieldName, fieldValue);
                actions.remove(user);
                actions.insert(user);
            }
        }

        return false;
    }
}
