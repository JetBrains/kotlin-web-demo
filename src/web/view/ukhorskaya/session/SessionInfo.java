package web.view.ukhorskaya.session;

import web.view.ukhorskaya.TimeManager;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 11/30/11
 * Time: 1:25 PM
 */

public class SessionInfo {
    public static TimeManager TIME_MANAGER = new TimeManager();
    public static int SESSION_ID = 0;
    public static TypeOfRequest TYPE = TypeOfRequest.GET_RESOURCE;

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
        WRITE_LOG, GET_RESOURCE
    }

}
