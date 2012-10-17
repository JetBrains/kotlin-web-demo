/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.webdemo;

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
        params.sessionId = ResponseUtils.substringBetween(requestStr, "kotlinServer?sessionId=", "&type=");
        params.type = ResponseUtils.substringBetween(requestStr, "&type=", "&args=");
        params.args = ResponseUtils.substringAfter(requestStr, "&args=");
        return params;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
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
