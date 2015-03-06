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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.psi.PsiFile;
import org.jetbrains.webdemo.*;
import org.jetbrains.webdemo.backend.responseHelpers.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MyHttpSession {
    private final BackendSessionInfo sessionInfo;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Project currentProject;
    private ObjectMapper objectMapper = new ObjectMapper();

    public MyHttpSession(BackendSessionInfo info) {
        this.sessionInfo = info;
    }

    public void handle(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            this.request = request;
            this.response = response;
            switch (request.getParameter("type")) {
                case ("run"):
                    sendExecutorResult();
                    break;
                case ("highlight"):
                    sendHighlightingResult();
                    break;
                case ("convertToKotlin"):
                    sessionInfo.setType(BackendSessionInfo.TypeOfRequest.CONVERT_TO_KOTLIN);
                    sendConversationResult();
                    break;
                case ("complete"):
                    sessionInfo.setType(BackendSessionInfo.TypeOfRequest.COMPLETE);
                    sendCompletionResult();
                    break;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            if (sessionInfo != null && sessionInfo.getType() != null && currentProject != null) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), JsonUtils.toJson(currentProject));
            } else {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, "UNKNOWN", "unknown", "null");
            }
            writeResponse(ResponseUtils.getErrorInJson("Internal server error"), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendConversationResult() {
        writeResponse(new JavaToKotlinConverter(sessionInfo).getResult(request.getParameter("text")), HttpServletResponse.SC_OK);
    }

    private void sendExecutorResult() {
        try {
            currentProject = objectMapper.readValue(request.getParameter("project"), Project.class);
            List<PsiFile> psiFiles = createProjectPsiFiles(currentProject);
            sessionInfo.setRunConfiguration(currentProject.confType);
            if (sessionInfo.getRunConfiguration().equals(BackendSessionInfo.RunConfiguration.JAVA) || sessionInfo.getRunConfiguration().equals(BackendSessionInfo.RunConfiguration.JUNIT)) {
                sessionInfo.setType(BackendSessionInfo.TypeOfRequest.RUN);

                CompileAndRunExecutor responseForCompilation = new CompileAndRunExecutor(psiFiles, Initializer.getInstance().getEnvironment().getProject(), sessionInfo, currentProject.args);
                writeResponse(responseForCompilation.getResult(), HttpServletResponse.SC_OK);
            } else {
                sessionInfo.setType(BackendSessionInfo.TypeOfRequest.CONVERT_TO_JS);
                writeResponse(new JsConverter(sessionInfo).getResult(psiFiles, sessionInfo, currentProject.args), HttpServletResponse.SC_OK);
            }
        } catch (IOException e) {
            writeResponse("Can't parse project", HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private List<PsiFile> createProjectPsiFiles(Project example) {
        List<PsiFile> result = new ArrayList<>();
        for (ProjectFile file : example.files) {
            result.add(JetPsiFactoryUtil.createFile(Initializer.getInstance().getEnvironment().getProject(), file.getName(), file.getText()));
        }
        return result;
    }

    public void sendCompletionResult() {
        try {
            String fileName = request.getParameter("filename");
            int line = Integer.parseInt(request.getParameter("line"));
            int ch = Integer.parseInt(request.getParameter("ch"));
            currentProject = objectMapper.readValue(request.getParameter("project"), Project.class);
            List<PsiFile> psiFiles = createProjectPsiFiles(currentProject);
            sessionInfo.setRunConfiguration(currentProject.confType);

            JsonResponseForCompletion jsonResponseForCompletion = new JsonResponseForCompletion(psiFiles, sessionInfo, fileName, line, ch);
            writeResponse(jsonResponseForCompletion.getResult(), HttpServletResponse.SC_OK);
        } catch (IOException e) {
            writeResponse("Can't parse project", HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    public void sendHighlightingResult() {
        sessionInfo.setType(BackendSessionInfo.TypeOfRequest.HIGHLIGHT);
        try {
            currentProject = objectMapper.readValue(request.getParameter("project"), Project.class);
            sessionInfo.setRunConfiguration(currentProject.confType);
            List<PsiFile> psiFiles = createProjectPsiFiles(currentProject);
            JsonResponseForHighlighting responseForHighlighting = new JsonResponseForHighlighting(psiFiles, sessionInfo, Initializer.getInstance().getEnvironment().getProject());
            String response = responseForHighlighting.getResult();
            response = response.replaceAll("\\n", "");
            writeResponse(response, HttpServletResponse.SC_OK);
        } catch (IOException e) {
            writeResponse("Can't parse project", HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    //Send Response
    private void writeResponse(String responseBody, int errorCode) {
        try {
            ResponseUtils.writeResponse(request, response, responseBody, errorCode);
            LogWriter.logBackendRequestInfo(
                    sessionInfo.getType(),
                    "runConf=" + currentProject.confType + " time=" + sessionInfo.getTimeManager().getMillisecondsFromStart()
            );
        } catch (IOException e) {
            //This is an exception we can't send data to client
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), JsonUtils.toJson(currentProject));
        }
    }


}
