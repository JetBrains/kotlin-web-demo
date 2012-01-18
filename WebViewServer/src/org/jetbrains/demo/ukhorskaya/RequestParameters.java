package org.jetbrains.demo.ukhorskaya;

import com.intellij.openapi.util.Pair;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 1/18/12
 * Time: 11:36 AM
 */

public class RequestParameters {
    private String sessionId;
    private String type;
    private String args;
    
    private RequestParameters() {
        
    }
    
    public static RequestParameters parseRequest(String requestStr) {
        RequestParameters params = new RequestParameters();
        params.sessionId = ResponseUtils.substringBetween(requestStr, "kotlinServer?sessionId=", "&type=");
        params.type = ResponseUtils.substringBetween(requestStr, "&type=", "&args=");
        params.args = ResponseUtils.substringAfter(requestStr, "&args=");
        return params;
    }

    public static RequestParameters parseRequestWoQuery(String requestStr) {
        RequestParameters params = new RequestParameters();
        params.sessionId = ResponseUtils.substringBetween(requestStr, "kotlinServer&sessionId=", "&type=");
        params.type = ResponseUtils.substringBetween(requestStr, "&type=", "&args=");
        params.args = ResponseUtils.substringAfter(requestStr, "&args=");
        return params;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getType() {
        return type;
    }

    public String getArgs() {
        return args;
    }
    
    public boolean compareType(String type) {
        return this.type.equals(type);
    }
}
