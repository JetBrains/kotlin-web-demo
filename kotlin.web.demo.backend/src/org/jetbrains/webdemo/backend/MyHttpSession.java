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
import org.jetbrains.webdemo.backend.responseHelpers.CompileAndRunExecutor;
import org.jetbrains.webdemo.backend.responseHelpers.JavaToKotlinConverter;
import org.jetbrains.webdemo.backend.responseHelpers.JsConverter;
import org.jetbrains.webdemo.examplesLoader.Project;
import org.jetbrains.webdemo.examplesLoader.ProjectFile;
import org.jetbrains.webdemo.handlers.ServerResponseUtils;
import org.jetbrains.webdemo.responseHelpers.*;
import org.jetbrains.webdemo.session.SessionInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyHttpSession {
    private final SessionInfo sessionInfo;
    private final Map<String, String[]> parameters;
    protected com.intellij.openapi.project.Project currentProject;
    protected PsiFile currentPsiFile;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ObjectMapper objectMapper = new ObjectMapper();

    public MyHttpSession(SessionInfo info, Map<String, String[]> parameters) {
        this.sessionInfo = info;
        this.parameters = parameters;
    }

    public void handle(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            this.request = request;
            this.response = response;
            String param = request.getRequestURI() + "?" + request.getQueryString();

            ErrorWriterOnServer.LOG_FOR_INFO.info("request: " + param + " ip: " + sessionInfo.getId());

            switch (parameters.get("type")[0]) {
                case ("run"):
                    ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getType()));
                    sendExecutorResult();
                    break;
                case ("highlight"):
                    sendHighlightingResult();
                    break;
                case ("convertToKotlin"):
                    sessionInfo.setType(SessionInfo.TypeOfRequest.CONVERT_TO_KOTLIN);
                    sendConversationResult();
                    break;
                case ("complete"):
                    sessionInfo.setType(SessionInfo.TypeOfRequest.COMPLETE);
                    ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getType()));
                    sendCompletionResult();
                    break;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            if (sessionInfo != null && sessionInfo.getType() != null && currentPsiFile != null && currentPsiFile.getText() != null) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
            } else {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, "UNKNOWN", "unknown", "null");
            }
            writeResponse(ResponseUtils.getErrorInJson("Internal server error"), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendConversationResult() {
        writeResponse(new JavaToKotlinConverter(sessionInfo).getResult(parameters.get("text")[0]), HttpServletResponse.SC_OK);
    }

    private void sendExecutorResult() {
        try {
            Project project = objectMapper.readValue(parameters.get("project")[0], Project.class);
            List<PsiFile> psiFiles = createProjectPsiFiles(project);
            sessionInfo.setRunConfiguration(project.confType);
            if (sessionInfo.getRunConfiguration().equals(SessionInfo.RunConfiguration.JAVA) || sessionInfo.getRunConfiguration().equals(SessionInfo.RunConfiguration.JUNIT)) {
                sessionInfo.setType(SessionInfo.TypeOfRequest.RUN);

                CompileAndRunExecutor responseForCompilation = new CompileAndRunExecutor(psiFiles, currentProject, sessionInfo, project.args);
                writeResponse(responseForCompilation.getResult(), HttpServletResponse.SC_OK);
            } else {
                sessionInfo.setType(SessionInfo.TypeOfRequest.CONVERT_TO_JS);
                writeResponse(new JsConverter(sessionInfo).getResult(psiFiles, project.args), HttpServletResponse.SC_OK);
            }
        } catch (IOException e) {
            writeResponse("Can't parse project", HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private List<PsiFile> createProjectPsiFiles(Project example) {
        List<PsiFile> result = new ArrayList<>();
        currentProject = Initializer.INITIALIZER.getEnvironment().getProject();
        for (ProjectFile file : example.files) {
            result.add(JetPsiFactoryUtil.createFile(currentProject, file.getName(), file.getText()));
        }
        return result;
    }

    public void sendCompletionResult() {
        try {
            String fileName = parameters.get("filename")[0];
            int line = Integer.parseInt(parameters.get("line")[0]);
            int ch = Integer.parseInt(parameters.get("ch")[0]);
            Project project = objectMapper.readValue(parameters.get("project")[0], Project.class);
            List<PsiFile> psiFiles = createProjectPsiFiles(project);
            sessionInfo.setRunConfiguration(project.confType);

            JsonResponseForCompletion jsonResponseForCompletion = new JsonResponseForCompletion(psiFiles, sessionInfo, fileName, line, ch);
            writeResponse(jsonResponseForCompletion.getResult(), HttpServletResponse.SC_OK);
        } catch (IOException e) {
            writeResponse("Can't parse project", HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    public void sendHighlightingResult() {
        sessionInfo.setType(SessionInfo.TypeOfRequest.HIGHLIGHT);
        try {
            Project project = objectMapper.readValue(parameters.get("project")[0], Project.class);
            sessionInfo.setRunConfiguration(project.confType);
            List<PsiFile> psiFiles = createProjectPsiFiles(project);
            JsonResponseForHighlighting responseForHighlighting = new JsonResponseForHighlighting(psiFiles, sessionInfo, currentProject);
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
            ServerResponseUtils.writeResponse(request, response, responseBody, errorCode);
            ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(),
                    sessionInfo.getId(), "ALL " + sessionInfo.getTimeManager().getMillisecondsFromStart() + " request=" + request.getRequestURI() + "?" + request.getQueryString()));
        } catch (IOException e) {
            //This is an exception we can't send data to client
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
        }
    }


}
