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
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.Project;
import org.jetbrains.webdemo.ProjectFile;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.backend.responseHelpers.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyHttpSession {
    private final BackendSessionInfo sessionInfo;
    protected com.intellij.openapi.project.Project currentProject;
    protected PsiFile currentPsiFile;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ObjectMapper objectMapper = new ObjectMapper();

    public MyHttpSession(BackendSessionInfo info) {
        this.sessionInfo = info;
    }

    public void handle(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            this.request = request;
            this.response = response;
            String param = request.getRequestURI() + "?" + request.getQueryString();

            ErrorWriter.LOG_FOR_INFO.info("request: " + param + " ip: " + sessionInfo.getId());

            switch (request.getParameter("type")) {
                case ("run"):
                    ErrorWriter.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(BackendSessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getType()));
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
                    ErrorWriter.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(BackendSessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getType()));
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
        writeResponse(new JavaToKotlinConverter(sessionInfo).getResult(request.getParameter("text")), HttpServletResponse.SC_OK);
    }

    private void sendExecutorResult() {
        try {
            Project project = objectMapper.readValue(request.getParameter("project"), Project.class);
            List<PsiFile> psiFiles = createProjectPsiFiles(project);
            sessionInfo.setRunConfiguration(project.confType);
            if (sessionInfo.getRunConfiguration().equals(BackendSessionInfo.RunConfiguration.JAVA) || sessionInfo.getRunConfiguration().equals(BackendSessionInfo.RunConfiguration.JUNIT)) {
                sessionInfo.setType(BackendSessionInfo.TypeOfRequest.RUN);

                CompileAndRunExecutor responseForCompilation = new CompileAndRunExecutor(psiFiles, currentProject, sessionInfo, project.args);
                writeResponse(responseForCompilation.getResult(), HttpServletResponse.SC_OK);
            } else {
                sessionInfo.setType(BackendSessionInfo.TypeOfRequest.CONVERT_TO_JS);
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
        currentProject = Initializer.getInstance().getEnvironment().getProject();
        for (ProjectFile file : example.files) {
            result.add(JetPsiFactoryUtil.createFile(currentProject, file.getName(), file.getText()));
        }
        return result;
    }

    public void sendCompletionResult() {
        try {
            String fileName = request.getParameter("filename");
            int line = Integer.parseInt(request.getParameter("line"));
            int ch = Integer.parseInt(request.getParameter("ch"));
            Project project = objectMapper.readValue(request.getParameter("project"), Project.class);
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
        sessionInfo.setType(BackendSessionInfo.TypeOfRequest.HIGHLIGHT);
        try {
            Project project = objectMapper.readValue(request.getParameter("project"), Project.class);
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
            ResponseUtils.writeResponse(request, response, responseBody, errorCode);
            ErrorWriter.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(),
                    sessionInfo.getId(), "ALL " + sessionInfo.getTimeManager().getMillisecondsFromStart() + " request=" + request.getRequestURI() + "?" + request.getQueryString()));
        } catch (IOException e) {
            //This is an exception we can't send data to client
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
        }
    }


}
