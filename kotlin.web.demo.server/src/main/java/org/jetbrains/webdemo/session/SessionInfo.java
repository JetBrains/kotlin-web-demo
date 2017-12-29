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

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.webdemo.TimeManager;

@Getter
@Setter
public class SessionInfo {
    private final TimeManager timeManager = new TimeManager();
    private String id;
    private TypeOfRequest type = TypeOfRequest.GET_RESOURCE;
    private RunConfiguration runConfiguration = RunConfiguration.JAVA;
    private UserInfo userInfo = new UserInfo();
    private String originUrl = null;

    public SessionInfo(String sessionId) {
        this.id = sessionId;
    }

    public String getType() {
        return type.name();
    }

    public void setRunConfiguration(String runConfiguration) {
        if ("java".equals(runConfiguration)) {
            this.runConfiguration = RunConfiguration.JAVA;
        } else if ("js".equals(runConfiguration)) {
            this.runConfiguration = RunConfiguration.JS;
        } else if ("canvas".equals(runConfiguration)) {
            this.runConfiguration = RunConfiguration.CANVAS;
        } else if ("junit".equals(runConfiguration)) {
            this.runConfiguration = RunConfiguration.JUNIT;
        } else {
            this.runConfiguration = RunConfiguration.JAVA;
        }
    }

    public enum TypeOfRequest {
        DELETE_FILE,
        DELETE_PROJECT,
        LOAD_EXAMPLE,
        GET_RESOURCE,
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


