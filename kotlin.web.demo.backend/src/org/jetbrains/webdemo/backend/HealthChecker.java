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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JacksonStdImpl;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.webdemo.backend.responseHelpers.CompileAndRunExecutor;
import org.jetbrains.webdemo.session.SessionInfo;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Semyon.Atamas on 2/13/2015.
 */
public class HealthChecker {
    private static HealthChecker instance = new HealthChecker();
    private ScheduledExecutorService periodicHealthChecker;
    private int status = HttpServletResponse.SC_OK;

    private HealthChecker(){
        periodicHealthChecker = new ScheduledThreadPoolExecutor(0);
        periodicHealthChecker.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                Project currentProject = Initializer.INITIALIZER.getEnvironment().getProject();
                PsiFile helloWorldFile = JetPsiFactoryUtil.createFile(
                        currentProject,
                        "HelloWorld.kt",
                        "fun main(args : Array<String>) {\n" +
                        "  println(\"Hello, world!\")\n" +
                        "}"
                );
                SessionInfo sessionInfo = new SessionInfo("test");
                sessionInfo.setRunConfiguration(SessionInfo.RunConfiguration.JAVA);
                CompileAndRunExecutor responseForCompilation = new CompileAndRunExecutor(Collections.singletonList(helloWorldFile), currentProject, sessionInfo, "");
                String result = responseForCompilation.getResult();
                try {
                    ArrayNode resultNodes = (ArrayNode) new ObjectMapper().readTree(result);
                    for(JsonNode resultNode : resultNodes){
                        if(resultNode.get("type").asText().equals("out")){
                            if(resultNode.get("text").asText().equals("&amp;lt;outStream&amp;gt;Hello, world!\r\n&amp;lt;/outStream&amp;gt;") && resultNode.get("exception").isNull()){
                                status = HttpServletResponse.SC_OK;
                            } else{
                                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 30, TimeUnit.SECONDS);
    }

    public static HealthChecker getInstance(){
        return instance;
    }

    public int getStatus(){
        return status;
    }

}
