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

package org.jetbrains.webdemo.sessions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.*;
import org.jetbrains.webdemo.database.DatabaseOperationException;
import org.jetbrains.webdemo.database.MySqlConnector;
import org.jetbrains.webdemo.examplesLoader.ExamplesList;
import org.jetbrains.webdemo.examplesLoader.Project;
import org.jetbrains.webdemo.examplesLoader.ProjectFile;
import org.jetbrains.webdemo.handlers.ServerHandler;
import org.jetbrains.webdemo.handlers.ServerResponseUtils;
import org.jetbrains.webdemo.responseHelpers.CompileAndRunExecutor;
import org.jetbrains.webdemo.responseHelpers.JavaToKotlinConverter;
import org.jetbrains.webdemo.responseHelpers.JsonResponseForCompletion;
import org.jetbrains.webdemo.responseHelpers.JsonResponseForHighlighting;
import org.jetbrains.webdemo.session.SessionInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyHttpSession {
    @NonNls
    private static final String[] REPLACES_REFS = {"&lt;", "&gt;", "&amp;", "&#39;", "&quot;"};
    @NonNls
    private static final String[] REPLACES_DISP = {"<", ">", "&", "'", "\""};
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

    public static String unescapeXml(@Nullable final String text) {
        if (text == null) return null;
        return StringUtil.replace(text, REPLACES_REFS, REPLACES_DISP);
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
                case ("loadHeaders"):
                    ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_EXAMPLES_LIST.name());
                    sendExamplesList(request, response);
                    break;
                case ("loadExample"):
                    sessionInfo.setType(SessionInfo.TypeOfRequest.LOAD_EXAMPLE);
                    ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getType()));
                    sendExampleContent();
                    break;
                case ("highlight"):
                    sendHighlightingResult();
                    break;
                case ("writeLog"):
                    sessionInfo.setType(SessionInfo.TypeOfRequest.WRITE_LOG);
                    sendWriteLogResult();
                    break;
                case ("convertToKotlin"):
                    sessionInfo.setType(SessionInfo.TypeOfRequest.CONVERT_TO_KOTLIN);
                    sendConversationResult();
                    break;
                case ("deleteProject"):
                    MySqlConnector.getInstance().deleteProject(sessionInfo.getUserInfo(), parameters.get("publicId")[0]);
                    break;
                case ("loadProject"):
                    sendLoadProjectResult();
                    break;
                case ("saveProject"):
                    sendSaveProjectResult();
                    break;
                case ("saveFile"):
                    sendSaveFileResult();
                    break;
                case ("addProject"):
                    sendAddProjectResult();
                    break;
                case ("addFile"):
                    sendAddFileResult();
                    break;
                case ("deleteFile"):
                    sendDeleteFileResult();
                    break;
                case ("renameFile"):
                    sendRenameFileResult();
                    break;
                case ("renameProject"):
                    sendRenameProjectResult();
                case ("complete"):
                    sessionInfo.setType(SessionInfo.TypeOfRequest.COMPLETE);
                    ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getType()));
                    sendCompletionResult();
                    break;
                case ("loadProjectInfoByFileId"):
                    sendLoadProjectInfoByFileIdResult();
                    break;
                case ("checkIfProjectExists"):
                    sendExistenceCheckResult();
                    break;
                default:
                    ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_RESOURCE.name() + " " + param);
                    sendResourceFile(request, response);
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

    private void sendExistenceCheckResult() {
        try {
            String id = parameters.get("publicId")[0];
            writeResponse(MySqlConnector.getInstance().isProjectExists(id) + "", HttpServletResponse.SC_OK);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void sendLoadProjectInfoByFileIdResult() {
        try {
            String id = parameters.get("id")[0];
            writeResponse(MySqlConnector.getInstance().getProjectHeaderInfoByPublicId(sessionInfo.getUserInfo(), id), HttpServletResponse.SC_OK);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void sendConversationResult() {
        writeResponse(new JavaToKotlinConverter(sessionInfo).getResult(parameters.get("text")[0]), HttpServletResponse.SC_OK);
    }

    private void sendWriteLogResult() {
        String type = parameters.get("args")[0];
        if (type.equals("info")) {
            ErrorWriterOnServer.LOG_FOR_INFO.info(parameters.get("text")[0]);
        } else if (type.equals("errorInKotlin")) {
            String tmp = parameters.get("text")[0];
            tmp = unescapeXml(unescapeXml(tmp));
            List<String> list = ErrorWriter.parseException(tmp);
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(list.get(2), list.get(3), list.get(1), "unknown", list.get(4));
        } else {
            String tmp = parameters.get("text")[0];
            List<String> list = ErrorWriter.parseException(tmp);
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(list.get(2), list.get(3), list.get(1), "unknown", list.get(4));
        }
        writeResponse("Data sent", HttpServletResponse.SC_OK);
    }

    private void sendAddFileResult() {
        try {
            String projectPublicId = parameters.get("publicId")[0];
            String fileName = parameters.get("filename")[0].replaceAll("_", " ");
            String id = MySqlConnector.getInstance().addFileToProject(sessionInfo.getUserInfo(), projectPublicId, fileName);
            writeResponse(id, HttpServletResponse.SC_OK);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private void sendAddProjectResult() {
        String[] content = parameters.get("content");
        try {
            if (content != null) {
                try {
                    Project project = objectMapper.readValue(content[0], Project.class);
                    project.name = parameters.get("args")[0].replaceAll("_", " "); //when user calls save as we must change project name
                    project.parent = "My Programs"; //to show that it is user project, not example
                    String publicId = MySqlConnector.getInstance().addProject(sessionInfo.getUserInfo(), project);
                    writeResponse(publicId, HttpServletResponse.SC_OK);
                } catch (IOException e) {
                    writeResponse("Can't parse file", HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                String publicIds = MySqlConnector.getInstance().addProject(sessionInfo.getUserInfo(), parameters.get("args")[0].replaceAll("_", " "));
                writeResponse(publicIds, HttpServletResponse.SC_OK);
            }
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private void sendRenameFileResult() {
        try {
            String publicId = parameters.get("publicId")[0];
            String newName = parameters.get("newName")[0].replaceAll("_", " ");
            newName = newName.endsWith(".kt") ? newName : newName + ".kt";
            MySqlConnector.getInstance().renameFile(sessionInfo.getUserInfo(), publicId, newName);
            writeResponse("ok", HttpServletResponse.SC_OK);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private void sendExamplesList(HttpServletRequest request, final HttpServletResponse response) {
        try {
            ObjectNode responseBody = new ObjectNode(JsonNodeFactory.instance);

            List<String> orderedFolderNames = ExamplesList.getInstance().getOrderedFolderNames();
            responseBody.put("orderedFolderNames", objectMapper.valueToTree(orderedFolderNames));
            for (String folderName : orderedFolderNames) {
                responseBody.put(folderName, objectMapper.valueToTree(ExamplesList.getInstance().getFolder(folderName).getOrderedExampleNames()));
            }

            if (sessionInfo.getUserInfo().isLogin()) {
                responseBody.put("My programs", MySqlConnector.getInstance().getProjectHeaders(sessionInfo.getUserInfo()));
            }
            writeResponse(request, response, responseBody.toString(), HttpServletResponse.SC_OK);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendDeleteFileResult() {
        try {
            String publicId = parameters.get("publicId")[0];
            MySqlConnector.getInstance().deleteFile(sessionInfo.getUserInfo(), publicId);
            writeResponse(HttpServletResponse.SC_OK);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private void sendLoadProjectResult() {
        try {
            String result;
            String id = parameters.get("publicId")[0];
            result = MySqlConnector.getInstance().getProjectContent(sessionInfo.getUserInfo(), id);
            writeResponse(result, HttpServletResponse.SC_OK);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void sendSaveProjectResult() {
        try {
            Project project = objectMapper.readValue(parameters.get("project")[0], Project.class);
            String publicId = parameters.get("publicId")[0];
            MySqlConnector.getInstance().saveProject(sessionInfo.getUserInfo(), publicId, project);
            writeResponse("ок", HttpServletResponse.SC_OK);
        } catch (IOException e) {
            writeResponse("Can't parse file", HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private void sendSaveFileResult() {
        try {
            String publicId = parameters.get("publicId")[0];

            ProjectFile file = objectMapper.readValue(parameters.get("file")[0], ProjectFile.class);
            MySqlConnector.getInstance().saveFile(sessionInfo.getUserInfo(), publicId, file);
            writeResponse("ok", HttpServletResponse.SC_OK);
        } catch (IOException e) {
            writeResponse("Can't parse file", HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private void sendExecutorResult() {
        try {

            Project example = objectMapper.readValue(parameters.get("project")[0], Project.class);
            if (example.originUrl != null) {
                addUnmodifiableDataToExample(example, example.originUrl);
            }

            sessionInfo.setRunConfiguration(example.confType);
            if (sessionInfo.getRunConfiguration().equals(SessionInfo.RunConfiguration.JAVA) || sessionInfo.getRunConfiguration().equals(SessionInfo.RunConfiguration.JUNIT)) {
                sessionInfo.setType(SessionInfo.TypeOfRequest.RUN);
                List<PsiFile> psiFiles = createProjectPsiFiles(example);

                CompileAndRunExecutor responseForCompilation = new CompileAndRunExecutor(psiFiles, currentProject, sessionInfo, example);
                writeResponse(responseForCompilation.getResult(), HttpServletResponse.SC_OK);
            } else {
                sessionInfo.setType(SessionInfo.TypeOfRequest.CONVERT_TO_JS);
//                writeResponse(new JsConverter(sessionInfo).getResult(data.text, consoleArgs), HttpServletResponse.SC_OK);
            }
        } catch (IOException e) {
            writeResponse("Can't parse project", HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void sendExampleContent() {
        writeResponse(ExamplesList.loadExample(parameters.get("args")[0].replaceAll("_", " "), parameters.get("name")[0].replaceAll("_", " ")), HttpServletResponse.SC_OK);
    }

    private List<PsiFile> createProjectPsiFiles(Project example) {
        List<PsiFile> result = new ArrayList<>();
        currentProject = Initializer.INITIALIZER.getEnvironment().getProject();
        for (ProjectFile file : example.files) {
            result.add(JetPsiFactoryUtil.createFile(currentProject, file.getName(), file.getContent()));
        }
        return result;
    }

    public void sendCompletionResult() {
        try {
            String fileName = parameters.get("filename")[0].replaceAll("_", " ");
            int line = Integer.parseInt(parameters.get("line")[0]);
            int ch = Integer.parseInt(parameters.get("ch")[0]);
            Project example = objectMapper.readValue(parameters.get("project")[0], Project.class);
            if (example.originUrl != null) {
                addUnmodifiableDataToExample(example, example.originUrl);
            }

            List<PsiFile> psiFiles = createProjectPsiFiles(example);
            sessionInfo.setRunConfiguration(parameters.get("runConf")[0]);

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
        sessionInfo.setRunConfiguration(parameters.get("args")[0]);
        try {
            Project example = objectMapper.readValue(parameters.get("project")[0], Project.class);
            if (example.originUrl != null) {
                addUnmodifiableDataToExample(example, example.originUrl);
            }
            List<PsiFile> psiFiles = createProjectPsiFiles(example);
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

    public void sendRenameProjectResult() {
        try {
            String publicId = parameters.get("publicId")[0];
            String newName = parameters.get("newName")[0].replaceAll("_", " ");
            MySqlConnector.getInstance().renameProject(sessionInfo.getUserInfo(), publicId, newName);
            writeResponse(HttpServletResponse.SC_OK);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        }

    }

    private void writeResponse(int errorCode) {
        writeResponse("ok", errorCode);
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


    private Project addUnmodifiableDataToExample(Project exampleObject, String originUrl) {
        Project storedExample = ExamplesList.getExample(originUrl);
        for (ProjectFile file : storedExample.files) {
            if (!file.isModifiable()) {
                exampleObject.files.add(file);
            }
        }
        exampleObject.testClasses = storedExample.testClasses;
        return exampleObject;
    }


    private void sendResourceFile(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getRequestURI() + "?" + request.getQueryString();
        path = ResponseUtils.substringAfterReturnAll(path, "resources");
        ErrorWriterOnServer.LOG_FOR_INFO.error(ErrorWriter.getInfoForLogWoIp(SessionInfo.TypeOfRequest.GET_RESOURCE.name(), "-1", "Resource doesn't downloaded from nginx: " + path));
        if (path.equals("")) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(
                    new UnsupportedOperationException("Empty path to resource"),
                    SessionInfo.TypeOfRequest.GET_RESOURCE.name(), request.getHeader("Origin"), path);
            writeResponse(request, response, "Path to the file is incorrect.", HttpServletResponse.SC_NOT_FOUND);
            return;
        } else if (path.startsWith("/messages/")) {
            writeResponse(request, response, "", HttpServletResponse.SC_OK);
            return;
        } else if (path.equals("/") || path.equals("/index.html")) {
            path = "/index.html";
            StringBuilder responseStr = new StringBuilder();
            InputStream is = null;
            try {
                is = ServerHandler.class.getResourceAsStream(path);
                responseStr.append(ResponseUtils.readData(is, true));
            } catch (FileNotFoundException e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                        SessionInfo.TypeOfRequest.GET_RESOURCE.name(), request.getHeader("Origin"), "index.html not found");
                writeResponse(request, response, "Cannot open this page", HttpServletResponse.SC_BAD_GATEWAY);
                return;
            } catch (IOException e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                        SessionInfo.TypeOfRequest.GET_RESOURCE.name(), request.getHeader("Origin"), "index.html not found");
                writeResponse(request, response, "Cannot open this page", HttpServletResponse.SC_BAD_GATEWAY);
                return;
            } finally {
                ServerResponseUtils.close(is);
            }

            OutputStream os = null;
            try {
                os = response.getOutputStream();
                os.write(responseStr.toString().getBytes());
            } catch (IOException e) {
                //This is an exception we can't send data to client
            } finally {
                ServerResponseUtils.close(os);
            }
            return;
        }

        InputStream is = ServerHandler.class.getResourceAsStream(path);
        if (is == null) {
            if (request.getQueryString() != null) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(
                        new UnsupportedOperationException("Broken path to resource"),
                        SessionInfo.TypeOfRequest.GET_RESOURCE.name(), request.getHeader("Origin"), request.getRequestURI() + "?" + request.getQueryString());
            }
            writeResponse(request, response, ("Resource not found. " + path), HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            FileUtil.copy(is, response.getOutputStream());
        } catch (IOException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.GET_RESOURCE.name(), request.getHeader("Origin"), request.getRequestURI() + "?" + request.getQueryString());
            writeResponse(request, response, "Could not load the resource from the server", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void writeResponse(HttpServletRequest request, HttpServletResponse response, String responseBody, int errorCode) {
        try {
            ServerResponseUtils.writeResponse(request, response, responseBody, errorCode);
        } catch (IOException e) {
            //This is an exception we can't send data to client
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, "UNKNOWN", request.getHeader("Origin"), "null");
        }
    }
}
