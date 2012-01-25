package org.jetbrains.demo.ukhorskaya.session;

import org.jetbrains.demo.ukhorskaya.TimeManager;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/30/11
 * Time: 1:25 PM
 */

public class SessionInfo {
    private final TimeManager timeManager = new TimeManager();
    private String id;
    private TypeOfRequest type = TypeOfRequest.GET_RESOURCE;
    private UserInfo userInfo = new UserInfo();

    public SessionInfo(String sessionId, TypeOfRequest typeOfRequest) {
        this.id = sessionId;
        this.type = typeOfRequest;
    }

    public SessionInfo(String sessionId) {
        this.id = sessionId;
    }

    public void setType(TypeOfRequest typeOfRequest) {
        this.type = typeOfRequest;
    }

    public TimeManager getTimeManager() {
        return timeManager;
    }

    public String getId() {
        return id;
    }

    public void setId(String sessionId) {
        this.id = sessionId;
    }

    public String getType() {
        return type.name();
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public enum TypeOfRequest {
        LOAD_ROOT,
        HIGHLIGHT,
        COMPLETE,
        RUN,
        LOAD_EXAMPLE,
        SEND_USER_DATA,
        GET_LOGS_LIST,
        DOWNLOAD_LOG,
        GET_EXAMPLES_LIST,
        GET_HELP_FOR_EXAMPLES,
        GET_HELP_FOR_WORDS,
        WRITE_LOG, GET_RESOURCE,
        ANALYZE_LOG,
        INC_NUMBER_OF_REQUESTS,
        CONVERT_TO_KOTLIN,
        CONVERT_TO_JS,
        SAVE_PROGRAM
    }



}


