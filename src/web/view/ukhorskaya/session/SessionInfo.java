package web.view.ukhorskaya.session;

import web.view.ukhorskaya.TimeManager;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/30/11
 * Time: 1:25 PM
 */

public class SessionInfo {
    private final TimeManager timeManager = new TimeManager();
    private int id = 0;
    private TypeOfRequest type = TypeOfRequest.GET_RESOURCE;
    private String ip;

    public SessionInfo(int sessionId, String ip, TypeOfRequest typeOfRequest) {
        this.id = sessionId;
        this.type = typeOfRequest;
        this.ip = ip;
    }

    public SessionInfo(int sessionId, String ip) {
        this.id = sessionId;
        this.ip = ip;
    }

    public void setType(TypeOfRequest typeOfRequest) {
        this.type = typeOfRequest;
    }

    public TimeManager getTimeManager() {
        return timeManager;
    }

    public int getId() {
        return id;
    }

    public void setId(int sessionId) {
        this.id = sessionId;
    }

    public String getType() {
        return type.name();
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getIp() {
        return ip;
    }

    public enum TypeOfRequest {
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
        CONVERT_TO_JS
    }

}
