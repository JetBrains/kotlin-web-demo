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

package org.jetbrains.webdemo.session;

import org.jetbrains.webdemo.TimeManager;

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
    private RunConfiguration runConfiguration = RunConfiguration.JAVA;
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

    public RunConfiguration getRunConfiguration() {
        return runConfiguration;
    }

    public void setRunConfiguration(RunConfiguration runConfiguration) {
        this.runConfiguration = runConfiguration;
    }

    public void setRunConfiguration(String runConfiguration) {
        if ("java".equals(runConfiguration)) {
            this.runConfiguration = RunConfiguration.JAVA;
        } else if ("js".equals(runConfiguration)) {
            this.runConfiguration = RunConfiguration.JS;
        } else if ("canvas".equals(runConfiguration)) {
            this.runConfiguration = RunConfiguration.CANVAS;
        } else {
            this.runConfiguration = RunConfiguration.JAVA;
        }
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
        SAVE_PROGRAM,
        AUTHORIZATION,
        WORK_WITH_DATABASE
    }

    public enum RunConfiguration {
        JAVA,
        JS,
        CANVAS
    }


}


