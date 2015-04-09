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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.webdemo.backend.responseHelpers.CompileAndRunExecutor;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;

/**
 * Created by Semyon.Atamas on 2/13/2015.
 */
public class HealthChecker {
    private static HealthChecker instance = new HealthChecker();
    private Timer timer;
    private int status = HttpServletResponse.SC_SERVICE_UNAVAILABLE;

    private HealthChecker(){
        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Project currentProject = Initializer.getInstance().getEnvironment().getProject();
                PsiFile helloWorldFile = JetPsiFactoryUtil.createFile(
                        currentProject,
                        "HelloWorld.kt",
                        "fun main(args : Array<String>) {\n" +
                                "  println(\"Hello, world!\")\n" +
                                "}"
                );
                BackendSessionInfo sessionInfo = new BackendSessionInfo("test");
                sessionInfo.setRunConfiguration(BackendSessionInfo.RunConfiguration.JAVA);
                CompileAndRunExecutor responseForCompilation = new CompileAndRunExecutor(Collections.singletonList(helloWorldFile), currentProject, sessionInfo, "");
                try {
                    String result = responseForCompilation.getResult();
                    ArrayNode resultNodes = (ArrayNode) new ObjectMapper().readTree(result);
                    for (JsonNode resultNode : resultNodes) {
                        if (resultNode.get("type").asText().equals("out")) {
                            if (resultNode.get("text").asText().equals("&amp;lt;outStream&amp;gt;Hello, world!" + System.lineSeparator() + "&amp;lt;/outStream&amp;gt;") && resultNode.get("exception").isNull()) {
                                status = HttpServletResponse.SC_OK;
                            } else {
                                status = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    status = HttpServletResponse.SC_SERVICE_UNAVAILABLE;
                }
            }
        }, 0, 60000);
    }

    public static HealthChecker getInstance(){
        return instance;
    }

    public int getStatus(){
        return status;
    }

}
