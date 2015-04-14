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

package org.jetbrains.webdemo.sessions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.webdemo.*;
import org.jetbrains.webdemo.database.DatabaseOperationException;
import org.jetbrains.webdemo.database.MySqlConnector;
import org.jetbrains.webdemo.examples.ExamplesFolder;
import org.jetbrains.webdemo.examples.ExamplesUtils;
import org.jetbrains.webdemo.handlers.ServerHandler;
import org.jetbrains.webdemo.session.SessionInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class MyHttpSession {
    private final SessionInfo sessionInfo;
    private Project currentProject;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private ObjectMapper objectMapper = new ObjectMapper();

    public MyHttpSession(SessionInfo info) {
        this.sessionInfo = info;
    }

    public void handle(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            this.request = request;
            this.response = response;

            switch (request.getParameter("type")) {
                case ("highlight"):
                    forwardHighlightRequest();
                    break;
                case ("run"):
                    forwardRunRequest();
                    break;
                case ("complete"):
                    forwadrCompleteRequest();
                    break;
                case ("convertToKotlin"):
                    forwardConvertResult();
                    break;
                case ("loadHeaders"):
                    sendExamplesList();
                    break;
                case ("loadExample"):
                    sendExampleContent();
                    break;
                case ("loadExampleFile"):
                    sendExampleFileContent();
                    break;
                case ("deleteProject"):
                    sendDeleteProjectResult(request);
                    break;
                case ("loadProject"):
                    sendLoadProjectResult();
                    break;
                case ("loadProjectFile"):
                    sendProjectFileContent();
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
                case ("checkFileExistence"):
                    sendFileExistenceResult();
                    break;
                case ("renameFile"):
                    sendRenameFileResult();
                    break;
                case ("renameProject"):
                    sendRenameProjectResult();
                    break;
                case ("loadProjectInfoByFileId"):
                    sendLoadProjectInfoByFileIdResult();
                    break;
                case ("checkIfProjectExists"):
                    sendExistenceCheckResult();
                    break;
                default:
                    sendResourceFile();
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

    private void sendDeleteProjectResult(HttpServletRequest request) {
        try {
            sessionInfo.setType(SessionInfo.TypeOfRequest.DELETE_PROJECT);
            MySqlConnector.getInstance().deleteProject(sessionInfo.getUserInfo(), request.getParameter("publicId"));
            writeResponse(HttpServletResponse.SC_OK);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private void forwardConvertResult() {
        sessionInfo.setType(SessionInfo.TypeOfRequest.CONVERT_TO_KOTLIN);
        Map<String, String> postParameters = new HashMap<>();
        postParameters.put("text", request.getParameter("text"));
        forwardRequestToBackend(request, postParameters);
    }

    private void forwadrCompleteRequest() {
        sessionInfo.setType(SessionInfo.TypeOfRequest.RUN);
        try {
            currentProject = objectMapper.readValue(request.getParameter("project"), Project.class);
            sessionInfo.setRunConfiguration(currentProject.confType);
            ExamplesUtils.addUnmodifiableFilesToProject(currentProject);
            Map<String, String> postParameters = new HashMap<>();
            postParameters.put("project", objectMapper.writeValueAsString(currentProject));
            postParameters.put("filename", request.getParameter("filename"));
            postParameters.put("line", request.getParameter("line"));
            postParameters.put("ch", request.getParameter("ch"));
            forwardRequestToBackend(request, postParameters);
        } catch (IOException e) {
            writeResponse("Can't parse project", HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void forwardRunRequest() {
        sessionInfo.setType(SessionInfo.TypeOfRequest.RUN);
        try {
            currentProject = objectMapper.readValue(request.getParameter("project"), Project.class);
            sessionInfo.setRunConfiguration(currentProject.confType);
            ExamplesUtils.addUnmodifiableFilesToProject(currentProject);
            Map<String, String> postParameters = new HashMap<>();
            postParameters.put("project", objectMapper.writeValueAsString(currentProject));
            forwardRequestToBackend(request, postParameters);
        } catch (IOException e) {
            writeResponse("Can't parse project", HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void forwardHighlightRequest() {
        sessionInfo.setType(SessionInfo.TypeOfRequest.HIGHLIGHT);
        try {
            currentProject = objectMapper.readValue(request.getParameter("project"), Project.class);
            sessionInfo.setRunConfiguration(currentProject.confType);
            ExamplesUtils.addUnmodifiableFilesToProject(currentProject);
            Map<String, String> postParameters = new HashMap<>();
            postParameters.put("project", objectMapper.writeValueAsString(currentProject));
            forwardRequestToBackend(request, postParameters);
        } catch (IOException e) {
            writeResponse("Can't parse project", HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void forwardRequestToBackend(HttpServletRequest request, Map<String, String> postParameters) {
        final boolean hasoutbody = (request.getMethod().equals("POST"));

        try {
            final URL url = new URL("http://" + ApplicationSettings.BACKEND_URL + "/"
                    + (request.getQueryString() != null ? "?" + request.getQueryString() : ""));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            final Enumeration<String> headers = request.getHeaderNames();
            while (headers.hasMoreElements()) {
                final String header = headers.nextElement();
                final Enumeration<String> values = request.getHeaders(header);
                while (values.hasMoreElements()) {
                    final String value = values.nextElement();
                    conn.addRequestProperty(header, value);
                }
            }

            conn.setRequestMethod("POST");
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setUseCaches(false);
            conn.setDoOutput(hasoutbody);

            try (OutputStream requestBody = conn.getOutputStream()) {
                boolean first = true;
                for (String key : postParameters.keySet()) {
                    if (first) {
                        first = false;
                    } else {
                        requestBody.write('&');
                    }
                    requestBody.write(URLEncoder.encode(key, "UTF8").getBytes());
                    requestBody.write('=');
                    requestBody.write(URLEncoder.encode(postParameters.get(key), "UTF8").getBytes());
                }
            }

            StringBuilder responseBody = new StringBuilder();
            if (conn.getResponseCode() >= 400) {
                StringBuilder serverMessage = new StringBuilder();
                try (InputStream errorStream = conn.getErrorStream()) {
                    if (errorStream != null) {
                        byte[] buffer = new byte[1024];
                        while (true) {
                            final int read = errorStream.read(buffer);
                            if (read <= 0) break;
                            serverMessage.append(new String(buffer, 0, read));
                        }
                    }
                }


                switch (conn.getResponseCode()) {
                    case HttpServletResponse.SC_NOT_FOUND:
                        responseBody.append("Kotlin compile server not found");
                        break;
                    case HttpServletResponse.SC_SERVICE_UNAVAILABLE:
                        responseBody.append("Kotlin compile server is temporary overloaded");
                        break;
                    default:
                        responseBody = serverMessage;
                        break;
                }
            } else {
                try (InputStream inputStream = conn.getInputStream()) {
                    if (inputStream != null) {
                        byte[] buffer = new byte[1024];
                        while (true) {
                            final int read = inputStream.read(buffer);
                            if (read <= 0) break;
                            responseBody.append(new String(buffer, 0, read));
                        }
                    }
                }
            }
            writeResponse(responseBody.toString(), conn.getResponseCode());
        } catch (SocketTimeoutException e) {
            writeResponse("Compile server connection timeout", HttpServletResponse.SC_GATEWAY_TIMEOUT);
        } catch (Exception e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, "FORWARD_REQUEST_TO_BACKEND", "", "Can't forward request to Kotlin compile server");
            writeResponse("Can't send your request to Kotlin compile server", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendFileExistenceResult() {
        try {
            ObjectNode response = new ObjectNode(JsonNodeFactory.instance);
            if (MySqlConnector.getInstance().getFile(request.getParameter("publicId")) != null) {
                response.put("exists", true);
            } else {
                response.put("exists", false);
            }
            writeResponse(response.toString(), HttpServletResponse.SC_OK);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void sendProjectFileContent() {
        try {
            ProjectFile file = MySqlConnector.getInstance().getFile(request.getParameter("publicId"));
            if (file != null) {
                writeResponse(objectMapper.writeValueAsString(file), HttpServletResponse.SC_OK);
            } else {
                writeResponse("", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IOException e) {
            writeResponse("Can't write response", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void sendExampleFileContent() {
        try {
            ProjectFile file = ExamplesUtils.getExampleFile(request.getParameter("publicId"));
            writeResponse(objectMapper.writeValueAsString(file), HttpServletResponse.SC_OK);
        } catch (IOException e) {
            writeResponse("Can't write response", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (NullPointerException e) {
            writeResponse("Can't find file", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void sendExistenceCheckResult() {
        try {
            String id = request.getParameter("publicId");
            ObjectNode response = new ObjectNode(JsonNodeFactory.instance);
            response.put("exists", MySqlConnector.getInstance().isProjectExists(id));
            writeResponse(response.toString(), HttpServletResponse.SC_OK);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void sendLoadProjectInfoByFileIdResult() {
        try {
            String response;
            if (request.getParameter("project_id") == null) {
                String publicId = request.getParameter("publicId");
                response = MySqlConnector.getInstance().getProjectHeaderInfoByPublicId(sessionInfo.getUserInfo(), publicId, null);
            } else {
                String project_id = request.getParameter("project_id");
                response = MySqlConnector.getInstance().getProjectHeaderInfoByPublicId(sessionInfo.getUserInfo(), null, project_id);
            }
            if (response != null) {
                writeResponse(response, HttpServletResponse.SC_OK);
            } else {
                writeResponse("", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void sendAddFileResult() {
        try {
            String projectPublicId = request.getParameter("publicId");
            String fileName = request.getParameter("filename");
            String id = MySqlConnector.getInstance().addFileToProject(sessionInfo.getUserInfo(), projectPublicId, fileName);
            writeResponse(id, HttpServletResponse.SC_OK);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private void sendAddProjectResult() {
        try {
            String content = request.getParameter("content");
            if (content != null) {
                try {
                    currentProject = objectMapper.readValue(content, Project.class);
                    currentProject.name = request.getParameter("args"); //when user calls save as we must change project name
                    String publicId = MySqlConnector.getInstance().addProject(sessionInfo.getUserInfo(), currentProject);
                    ObjectNode result = new ObjectNode(JsonNodeFactory.instance);
                    result.put("publicId", publicId);
                    result.put("content", MySqlConnector.getInstance().getProjectContent(publicId));
                    writeResponse(result.toString(), HttpServletResponse.SC_OK);
                } catch (IOException e) {
                    writeResponse("Can't parse file", HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                String publicIds = MySqlConnector.getInstance().addProject(sessionInfo.getUserInfo(), request.getParameter("args"));
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
            String publicId = request.getParameter("publicId");
            String newName = request.getParameter("newName");
            newName = newName.endsWith(".kt") ? newName : newName + ".kt";
            MySqlConnector.getInstance().renameFile(sessionInfo.getUserInfo(), publicId, newName);
            writeResponse("ok", HttpServletResponse.SC_OK);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private void sendExamplesList() {
        try {
            ArrayNode responseBody = new ArrayNode(JsonNodeFactory.instance);

            for (ExamplesFolder folder : ExamplesFolder.ROOT_FOLDER.getChildFolders()) {
                addFolderContent(responseBody, folder);
            }

            ObjectNode myProgramsContent = responseBody.addObject();
            myProgramsContent.put("name", "My programs");
            myProgramsContent.put("id", "My%20programs");
            myProgramsContent.putArray("childFolders");
            if (sessionInfo.getUserInfo().isLogin()) {
                myProgramsContent.put("projects", MySqlConnector.getInstance().getProjectHeaders(sessionInfo.getUserInfo()));
            } else {
                myProgramsContent.putArray("projects");
            }
            writeResponse(responseBody.toString(), HttpServletResponse.SC_OK);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    private void addFolderContent(ArrayNode arrayNode, ExamplesFolder folder) {
        ObjectNode folderContent = arrayNode.addObject();
        folderContent.put("name", folder.getName());
        folderContent.put("id", folder.getId());
        ArrayNode exampleHeaders = folderContent.putArray("projects");
        for (Project example : folder.getExamples()) {
            ObjectNode exampleHeader = exampleHeaders.addObject();
            exampleHeader.put("name", example.name);
            exampleHeader.put("publicId", example.originUrl);
        }
        ArrayNode childFolders = folderContent.putArray("childFolders");

        for (ExamplesFolder childFolder : folder.getChildFolders()) {
            addFolderContent(childFolders, childFolder);
        }
    }

    private void sendDeleteFileResult() {
        try {
            sessionInfo.setType(SessionInfo.TypeOfRequest.DELETE_FILE);
            boolean modifiable = Boolean.parseBoolean(request.getParameter("modifiable"));
            if (modifiable) {
                String publicId = request.getParameter("fileId");
                MySqlConnector.getInstance().deleteFile(sessionInfo.getUserInfo(), publicId);
            } else {
                String fileName = request.getParameter("fileName");
                String projectId = request.getParameter("projectId");
                MySqlConnector.getInstance().deleteUnmodifiableFile(sessionInfo.getUserInfo(), fileName, projectId);
            }
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
            String id = request.getParameter("publicId");
            result = MySqlConnector.getInstance().getProjectContent(id);
            if (result != null) {
                writeResponse(result, HttpServletResponse.SC_OK);
            } else {
                writeResponse("", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void sendSaveProjectResult() {
        try {
            currentProject = objectMapper.readValue(request.getParameter("project"), Project.class);
            String publicId = request.getParameter("publicId");
            MySqlConnector.getInstance().saveProject(sessionInfo.getUserInfo(), publicId, currentProject);
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
            ProjectFile file = objectMapper.readValue(request.getParameter("file"), ProjectFile.class);
            MySqlConnector.getInstance().saveFile(sessionInfo.getUserInfo(), file);
            writeResponse("ok", HttpServletResponse.SC_OK);
        } catch (IOException e) {
            writeResponse("Can't parse file", HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private void sendExampleContent() {
        try {
            Project example = ExamplesUtils.getExample(request.getParameter("publicId"));
            writeResponse(objectMapper.writeValueAsString(example), HttpServletResponse.SC_OK);
        } catch (IOException e) {
            writeResponse("Can't write response", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (NullPointerException e) {
            writeResponse("Can't find example", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    public void sendRenameProjectResult() {
        try {
            String publicId = request.getParameter("publicId");
            String newName = request.getParameter("newName");
            MySqlConnector.getInstance().renameProject(sessionInfo.getUserInfo(), publicId, newName);
            writeResponse(HttpServletResponse.SC_OK);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        }

    }

    private void writeResponse(int errorCode) {
        writeResponse("", errorCode);
    }

    //Send Response
    private void writeResponse(String responseBody, int statusCode) {
        try {
            ResponseUtils.writeResponse(request, response, responseBody, statusCode);
            if (currentProject != null) {
                LogWriter.logRequestInfo(
                        sessionInfo.getId(),
                        sessionInfo.getType(),
                        statusCode,
                        "runConf=" + currentProject.confType + " time=" + sessionInfo.getTimeManager().getMillisecondsFromStart()
                );
            } else {
                LogWriter.logRequestInfo(
                        sessionInfo.getId(),
                        sessionInfo.getType(),
                        statusCode,
                        "time=" + sessionInfo.getTimeManager().getMillisecondsFromStart() + " request=" + request.getRequestURI() + "?" + request.getQueryString());
            }
        } catch (IOException e) {
            //This is an exception we can't send data to client
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), JsonUtils.toJson(currentProject));
        }
    }


    private void sendResourceFile() {
        String path = request.getRequestURI() + "?" + request.getQueryString();
        path = ResponseUtils.substringAfterReturnAll(path, "resources");
//        ErrorWriter.LOG_FOR_INFO.error(ErrorWriter.getInfoForLogWoIp(SessionInfo.TypeOfRequest.GET_RESOURCE.name(), "-1", "Resource doesn't downloaded from nginx: " + path));
        if (path.equals("")) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(
                    new UnsupportedOperationException("Empty path to resource"),
                    SessionInfo.TypeOfRequest.GET_RESOURCE.name(), request.getHeader("Origin"), path);
            writeResponse("Path to the file is incorrect.", HttpServletResponse.SC_NOT_FOUND);
            return;
        } else if (path.startsWith("/messages/")) {
            writeResponse("", HttpServletResponse.SC_OK);
            return;
        } else if (path.equals("/") || path.equals("/index.html")) {
            path = "/index.html";
            StringBuilder responseStr = new StringBuilder();
            try (InputStream is = ServerHandler.class.getResourceAsStream(path)) {
                responseStr.append(ResponseUtils.readData(is, true));
            } catch (FileNotFoundException e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                        SessionInfo.TypeOfRequest.GET_RESOURCE.name(), request.getHeader("Origin"), "index.html not found");
                writeResponse("Cannot open this page", HttpServletResponse.SC_BAD_GATEWAY);
                return;
            } catch (IOException e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                        SessionInfo.TypeOfRequest.GET_RESOURCE.name(), request.getHeader("Origin"), "index.html not found");
                writeResponse("Cannot open this page", HttpServletResponse.SC_BAD_GATEWAY);
                return;
            }

            try (OutputStream os = response.getOutputStream()) {
                os.write(responseStr.toString().getBytes());
            } catch (IOException e) {
                //This is an exception we can't send data to client
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
            writeResponse(("Resource not found. " + path), HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            FileUtil.copy(is, response.getOutputStream());
        } catch (IOException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.GET_RESOURCE.name(), request.getHeader("Origin"), request.getRequestURI() + "?" + request.getQueryString());
            writeResponse("Could not load the resource from the server", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

}
