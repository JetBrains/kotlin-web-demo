package org.jetbrains.demo.ukhorskaya.database;

import com.mongodb.*;
import org.apache.commons.lang.math.RandomUtils;
import org.bson.types.ObjectId;
import org.jetbrains.demo.ukhorskaya.ErrorWriter;
import org.jetbrains.demo.ukhorskaya.ErrorWriterOnServer;
import org.jetbrains.demo.ukhorskaya.ResponseUtils;
import org.jetbrains.demo.ukhorskaya.handlers.ServerHandler;
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

    public String addProgramInfo(UserInfo userInfo, String programName, String programText, String args) {
        try {
            if (findUser(userInfo)) {

                if (!checkCountOfPrograms(userInfo)) {
                    return ResponseUtils.getJsonString("exception", "You can save only 100 programs");
                }
                if (findProgramByName(userInfo, programName)) {
                    return ResponseUtils.getJsonString("exception", "Program with same name already exists. Please choose the another one.");
                }


                DBCollection programIdProgramInfo = database.getCollection("programIdProgramInfo");
                BasicDBObject program = new BasicDBObject();
                program.put("name", programName);
                program.put("text", programText);
                program.put("args", args);
                program.put("link", "");
                programIdProgramInfo.insert(program);

                ObjectId programId = (ObjectId) program.get("_id");

                DBCollection userIdProgramId = database.getCollection("userIdProgramId");
                BasicDBObject field = new BasicDBObject();
                field.put("id", userInfo.getId());
                field.put("type", userInfo.getType());
                field.put("programId", programId);
                userIdProgramId.insert(field);
                return ResponseUtils.getJsonString("programName", programName + "&id=" + programId);
            } else {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                        SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), "Cannot find user at userIdUserInfo table",
                        userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName()));
                return ResponseUtils.getJsonString("exception", "Please, login.");
            }
        } catch (Throwable e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                    SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), e,
                    userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName()));
            return ResponseUtils.getJsonString("exception", "Unknown error until saving your program");
        }
    }

    public boolean checkCountOfPrograms(UserInfo userInfo) {
        BasicDBObject user = new BasicDBObject();
        user.put("id", userInfo.getId());
        user.put("type", userInfo.getType());

        DBCollection userIdProgramId = database.getCollection("userIdProgramId");
        DBCursor cursor = userIdProgramId.find(user);

        return cursor.count() < 100;
    }

    public String getPublicLink(String programId) {
        ObjectId programIdObject = new ObjectId(programId);
        DBCollection programIdProgramInfo = database.getCollection("programIdProgramInfo");
        DBObject program = programIdProgramInfo.findOne(new BasicDBObject("_id", programIdObject));

        String publicLink = (String) program.get("link");
        if (publicLink == null || publicLink.isEmpty()) {
            publicLink = "http://" + ServerHandler.HOST + "/?publicLink=" + programId;
            program.put("link", publicLink);
            programIdProgramInfo.findAndModify(new BasicDBObject("_id", programIdObject), program);
        }

        return ResponseUtils.getJsonString("text", publicLink);
    }

    public String getProgramTextByPublicLink(String programId) {
        try {
            DBCollection programIdProgramInfo = database.getCollection("programIdProgramInfo");
            BasicDBObject program = new BasicDBObject();
            program.put("_id", new ObjectId(programId));
            DBObject result = programIdProgramInfo.findOne(program);
            if (result == null) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                        SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), "Cannot find program by id in programIdProgramInfo",
                        programId));
                return ResponseUtils.getJsonString("exception", "Cannot find program by this link");
            }
            String link = (String) result.get("link");
            if (link != null && !link.isEmpty()) {
                JSONArray array = new JSONArray();
                Map<String, String> map = new HashMap<String, String>();
                map.put("text", (String) result.get("text"));
                String args = (String) result.get("args");
                if (args == null) {
                    args = "";
                }
                map.put("args", args);
                array.put(map);
                return array.toString();

            } else {
                return ResponseUtils.getJsonString("exception", "Link for this program is not public");
            }
        } catch (Throwable e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                    SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), e,
                    programId));
            return ResponseUtils.getJsonString("exception", "Unknown error until loading program by link");
        }
    }

    public String resaveProgram(String programId, String programText, String args) {
        try {
            ObjectId programIdObject = new ObjectId(programId);
            DBCollection programIdProgramInfo = database.getCollection("programIdProgramInfo");
            DBObject object = programIdProgramInfo.findOne(new BasicDBObject("_id", programIdObject));
            if (object == null) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                        SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), "Cannot find program by id in programIdProgramInfo",
                        programId));
                return ResponseUtils.getJsonString("exception", "Unknown error until saving your program");
            }
            object.put("text", programText);
            object.put("args", args);
            programIdProgramInfo.findAndModify(new BasicDBObject("_id", programIdObject), object);
            return ResponseUtils.getJsonString("programId", programId);
        } catch (Throwable e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                    SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), e,
                    programId));
            return ResponseUtils.getJsonString("exception", "Unknown error until saving your program");
        }
    }

    public boolean findProgram(UserInfo info, String programId) {
        DBCollection userIdProgramId = database.getCollection("userIdProgramId");
        BasicDBObject programWithId = new BasicDBObject();
        programWithId.put("programId", programId);

        DBCursor cursor = userIdProgramId.find(programWithId);
        while (cursor.hasNext()) {
            DBObject object = cursor.next();
            if (object.get("id").equals(info.getId()) && object.get("type").equals(info.getType())) {
                return true;
            }
        }
        return false;
    }

    public boolean findProgramByName(UserInfo userInfo, String programName) {
        BasicDBObject user = new BasicDBObject();
        user.put("id", userInfo.getId());
        user.put("type", userInfo.getType());

        DBCollection userIdProgramId = database.getCollection("userIdProgramId");
        DBCursor cursor = userIdProgramId.find(user);

        DBCollection programIdProgramInfo = database.getCollection("programIdProgramInfo");
        while (cursor.hasNext()) {
            DBObject object = cursor.next();
            if (object == null) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                        SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), "Cannot find user by userId in programIdProgramInfo",
                        userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName()));
                return false;
            }
            DBObject program = programIdProgramInfo.findOne(new BasicDBObject("_id", object.get("programId")));
            if (program.get("name").equals(programName)) {
                return true;
            }
        }
        return false;
    }

    public String getProgramText(String programId) {
        try {
            DBCollection programIdProgramInfo = database.getCollection("programIdProgramInfo");
            BasicDBObject program = new BasicDBObject();
            ObjectId programIdObject;
            try {
                programIdObject = new ObjectId(programId);
            } catch (IllegalArgumentException e) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                        SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), e,
                        programId));
                return ResponseUtils.getJsonString("exception", "Impossible to find a program.");
            }
            program.put("_id", programIdObject);

            DBObject result = programIdProgramInfo.findOne(program);
            if (result == null) {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                        SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), "Cannot find program by id in programIdProgramInfo",
                        programId));
                return ResponseUtils.getJsonString("exception", "Unknown error until loading your program");
            }
            JSONArray array = new JSONArray();
            Map<String, String> map = new HashMap<String, String>();
            map.put("type", "text");
            map.put("text", (String) result.get("text"));
            String args = (String) result.get("args");
            if (args == null) {
                args = "";
            }
            map.put("args", args);
            array.put(map);
            return array.toString();
        } catch (Throwable e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                    SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), e,
                    programId));
            return ResponseUtils.getJsonString("exception", "Unknown error until loading your program");
        }
    }

    public String getListOfProgramsForUser(UserInfo userInfo) {
        try {
            BasicDBObject user = new BasicDBObject();
            user.put("id", userInfo.getId());
            user.put("type", userInfo.getType());

            DBCollection userIdProgramId = database.getCollection("userIdProgramId");
            DBCursor cursor = userIdProgramId.find(user);
            JSONArray result = new JSONArray();
            DBCollection programIdProgramInfo = database.getCollection("programIdProgramInfo");
            while (cursor.hasNext()) {
                DBObject object = cursor.next();
                if (object == null) {
                    ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                            SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), "cannot find programs for user",
                            userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName()));
                    return ResponseUtils.getJsonString("exception", "Unknown error until loading list of your programs");
                }
                DBObject program = programIdProgramInfo.findOne(new BasicDBObject("_id", object.get("programId")));
                Map<String, String> map = new HashMap<String, String>();
                map.put("id", program.get("_id").toString());
                map.put("name", (String) program.get("name"));
                result.put(map);
            }
            return result.toString();
        } catch (Throwable e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                    SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), e,
                    userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName()));
            return ResponseUtils.getJsonString("exception", "Unknown error until loading list of your programs");
        }
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

    public String deleteProgram(UserInfo userInfo, String programId) {
        try {
            if (findUser(userInfo)) {

                DBCollection programIdProgramInfo = database.getCollection("programIdProgramInfo");

                ObjectId programIdObject = new ObjectId(programId);

                DBCollection userIdProgramId = database.getCollection("userIdProgramId");
                BasicDBObject field = new BasicDBObject();
                field.put("programId", programIdObject);
                DBObject userAndProgram = userIdProgramId.findOne(field);
                if (userAndProgram == null) {
                    ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                            SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), "Cannot find user for program",
                            userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + programId));
                    return ResponseUtils.getJsonString("exception", "Unknown error until deleting your program");
                }
                if (userAndProgram.get("id").equals(userInfo.getId()) && userAndProgram.get("type").equals(userInfo.getType())) {
                    programIdProgramInfo.remove(programIdProgramInfo.findOne(programIdObject));
                    userIdProgramId.remove(userAndProgram);
                    return ResponseUtils.getJsonString("text", "Program was successfully deleted.", programId);
                } else {
                    return ResponseUtils.getJsonString("exception", "Unknown error until deleting your program");
                }
            } else {
                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                        SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), "Cannot find user at userIdUserInfo table",
                        userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName()));
                return ResponseUtils.getJsonString("exception", "Unknown error until deleting your program");
            }
        } catch (Throwable e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(
                    SessionInfo.TypeOfRequest.SAVE_PROGRAM.name(), "Cannot find user for program",
                    userInfo.getId() + " " + userInfo.getType() + " " + userInfo.getName() + " " + programId));
            return ResponseUtils.getJsonString("exception", "Unknown error until saving your program");
        }
    }
}
