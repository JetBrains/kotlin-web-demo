/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package org.jetbrains.webdemo.backend;

import org.jetbrains.webdemo.TimeManager;

/**
 * Created by Semyon.Atamas on 2/13/2015.
 */
public class BackendSessionInfo {
    private final TimeManager timeManager = new TimeManager();
    private String id;
    private TypeOfRequest type = TypeOfRequest.GET_RESOURCE;
    private RunConfiguration runConfiguration = RunConfiguration.JAVA;
    private String originUrl = null;

    public BackendSessionInfo(String sessionId, TypeOfRequest typeOfRequest) {
        this.id = sessionId;
        this.type = typeOfRequest;
    }

    public BackendSessionInfo(String sessionId) {
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

    public String getType() {
        return type.name();
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
        } else if("junit".equals(runConfiguration)){
            this.runConfiguration = RunConfiguration.JUNIT;
        } else {
            this.runConfiguration = RunConfiguration.JAVA;
        }
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
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
        CANVAS,
        JUNIT
    }


}
