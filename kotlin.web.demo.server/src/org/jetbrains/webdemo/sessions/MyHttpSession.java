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
import org.jetbrains.webdemo.handlers.ServerHandler;
import org.jetbrains.webdemo.handlers.ServerResponseUtils;
import org.jetbrains.webdemo.session.SessionInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyHttpSession {
    @NonNls
    private static final String[] REPLACES_REFS = {"&lt;", "&gt;", "&amp;", "&#39;", "&quot;"};
    @NonNls
    private static final String[] REPLACES_DISP = {"<", ">", "&", "'", "\""};
    private final SessionInfo sessionInfo;
    private final Map<String, String[]> parameters;
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

            ErrorWriter.LOG_FOR_INFO.info("request: " + param + " ip: " + sessionInfo.getId());

            switch (parameters.get("type")[0]) {
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
                    ErrorWriter.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_EXAMPLES_LIST.name());
                    sendExamplesList(request, response);
                    break;
                case ("loadExample"):
                    sessionInfo.setType(SessionInfo.TypeOfRequest.LOAD_EXAMPLE);
                    ErrorWriter.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), sessionInfo.getId(), sessionInfo.getType()));
                    sendExampleContent();
                    break;
                case ("loadExampleFile"):
                    sendExampleFileContent();
                    break;
                case ("writeLog"):
                    sessionInfo.setType(SessionInfo.TypeOfRequest.WRITE_LOG);
                    sendWriteLogResult();
                    break;
                case ("deleteProject"):
                    MySqlConnector.getInstance().deleteProject(sessionInfo.getUserInfo(), parameters.get("publicId")[0]);
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
                    ErrorWriter.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_RESOURCE.name() + " " + param);
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

    private void forwardConvertResult() {
        sessionInfo.setType(SessionInfo.TypeOfRequest.CONVERT_TO_KOTLIN);
        Map<String, String> postParameters = new HashMap<>();
        postParameters.put("text", request.getParameter("text"));
        forwardRequestToBackend(request, response, postParameters);
    }

    private void forwadrCompleteRequest() {
        sessionInfo.setType(SessionInfo.TypeOfRequest.RUN);
        try {
            Project project = objectMapper.readValue(parameters.get("project")[0], Project.class);
            sessionInfo.setRunConfiguration(project.confType);
            if (project.originUrl != null) {
                addUnmodifiableDataToExample(project, project.originUrl);
            }
            Map<String, String> postParameters = new HashMap<>();
            postParameters.put("project", objectMapper.writeValueAsString(project));
            postParameters.put("filename", request.getParameter("filename"));
            postParameters.put("line", request.getParameter("line"));
            postParameters.put("ch", request.getParameter("ch"));
            forwardRequestToBackend(request, response, postParameters);
        } catch (IOException e) {
            writeResponse("Can't parse project", HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void forwardRunRequest() {
        sessionInfo.setType(SessionInfo.TypeOfRequest.RUN);
        try {
            Project project = objectMapper.readValue(parameters.get("project")[0], Project.class);
            sessionInfo.setRunConfiguration(project.confType);
            if (project.originUrl != null) {
                addUnmodifiableDataToExample(project, project.originUrl);
            }
            Map<String, String> postParameters = new HashMap<>();
            postParameters.put("project", objectMapper.writeValueAsString(project));
            forwardRequestToBackend(request, response, postParameters);
        } catch (IOException e) {
            writeResponse("Can't parse project", HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void forwardHighlightRequest() {
        sessionInfo.setType(SessionInfo.TypeOfRequest.HIGHLIGHT);
        try {
            Project project = objectMapper.readValue(parameters.get("project")[0], Project.class);
            sessionInfo.setRunConfiguration(project.confType);
            if (project.originUrl != null) {
                addUnmodifiableDataToExample(project, project.originUrl);
            }
            Map<String, String> postParameters = new HashMap<>();
            postParameters.put("project", objectMapper.writeValueAsString(project));
            forwardRequestToBackend(request, response, postParameters);
        } catch (IOException e) {
            writeResponse("Can't parse project", HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void forwardRequestToBackend(HttpServletRequest request, HttpServletResponse response, Map<String, String> postParameters) {
        final boolean hasoutbody = (request.getMethod().equals("POST"));

        try {
            final URL url = new URL("http://" + ApplicationSettings.BACKEND_REDIRECT + "/"
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

            //conn.setFollowRedirects(false);  // throws AccessDenied exception
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

            response.setStatus(conn.getResponseCode());
            for (int i = 0; ; ++i) {
                final String header = conn.getHeaderFieldKey(i);
                if (header == null) break;
                final String value = conn.getHeaderField(i);
                response.setHeader(header, value);
            }

            if (conn.getResponseCode() != HttpServletResponse.SC_OK) {
                response.getOutputStream().write("Can't send your request to backend server: ".getBytes());
                String message;
                switch (conn.getResponseCode()) {
                    case HttpServletResponse.SC_NOT_FOUND:
                        message = "backend server not found";
                        break;
                    case HttpServletResponse.SC_SERVICE_UNAVAILABLE:
                        message = "backend server is temporary overloaded";
                        break;
                    default:
                        message = "";
                        break;
                }
                response.getOutputStream().write(message.getBytes());
            } else {
                byte[] buffer = new byte[1024];
                while (true) {
                    final int read = conn.getInputStream().read(buffer);
                    if (read <= 0) break;
                    response.getOutputStream().write(buffer, 0, read);
                }
            }
        } catch (Exception e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, "FORWARD_REQUEST_TO_BACKEND", "", "Can't forward request to backend server");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendFileExistenceResult() {
        try {
            ObjectNode response = new ObjectNode(JsonNodeFactory.instance);
            if (MySqlConnector.getInstance().getFile(parameters.get("publicId")[0]) != null) {
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
            ProjectFile file = MySqlConnector.getInstance().getFile(parameters.get("publicId")[0]);
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
            ProjectFile file = ExamplesList.getInstance().getExampleFile(parameters.get("publicId")[0]);
            writeResponse(objectMapper.writeValueAsString(file), HttpServletResponse.SC_OK);
        } catch (IOException e) {
            writeResponse("Can't write response", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (NullPointerException e) {
            writeResponse("Can't find file", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void sendExistenceCheckResult() {
        try {
            String id = parameters.get("publicId")[0];
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
            if (parameters.get("project_id") == null) {
                String publicId = parameters.get("publicId")[0];
                response = MySqlConnector.getInstance().getProjectHeaderInfoByPublicId(sessionInfo.getUserInfo(), publicId, null);
            } else {
                String project_id = parameters.get("project_id")[0];
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

    private void sendWriteLogResult() {
        String type = parameters.get("args")[0];
        if (type.equals("info")) {
            ErrorWriter.LOG_FOR_INFO.info(parameters.get("text")[0]);
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
            String fileName = parameters.get("filename")[0];
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
                    project.name = parameters.get("args")[0]; //when user calls save as we must change project name
                    project.parent = "My Programs"; //to show that it is user project, not example
                    String publicId = MySqlConnector.getInstance().addProject(sessionInfo.getUserInfo(), project);
                    ObjectNode result = new ObjectNode(JsonNodeFactory.instance);
                    result.put("publicId", publicId);
                    result.put("content", MySqlConnector.getInstance().getProjectContent(publicId));
                    writeResponse(result.toString(), HttpServletResponse.SC_OK);
                } catch (IOException e) {
                    writeResponse("Can't parse file", HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                String publicIds = MySqlConnector.getInstance().addProject(sessionInfo.getUserInfo(), parameters.get("args")[0]);
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
            String newName = parameters.get("newName")[0];
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
            boolean modifiable = Boolean.parseBoolean(parameters.get("modifiable")[0]);
            if (modifiable) {
                String publicId = parameters.get("fileId")[0];
                MySqlConnector.getInstance().deleteFile(sessionInfo.getUserInfo(), publicId);
            } else {
                String fileName = parameters.get("fileName")[0];
                String projectId = parameters.get("projectId")[0];
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
            String id = parameters.get("publicId")[0];
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
            ProjectFile file = objectMapper.readValue(parameters.get("file")[0], ProjectFile.class);
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
            Project example = ExamplesList.getInstance().getExample(parameters.get("publicId")[0]);
            writeResponse(objectMapper.writeValueAsString(example), HttpServletResponse.SC_OK);
        } catch (IOException e) {
            writeResponse("Can't write response", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (NullPointerException e) {
            writeResponse("Can't find example", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    public void sendRenameProjectResult() {
        try {
            String publicId = parameters.get("publicId")[0];
            String newName = parameters.get("newName")[0];
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
            ResponseUtils.writeResponse(request, response, responseBody, errorCode);
            ErrorWriter.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(),
                    sessionInfo.getId(), "ALL " + sessionInfo.getTimeManager().getMillisecondsFromStart() + " request=" + request.getRequestURI() + "?" + request.getQueryString()));
        } catch (IOException e) {
            //This is an exception we can't send data to client
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
        }
    }


    private Project addUnmodifiableDataToExample(Project project, String originUrl) {
        Project storedExample = ExamplesList.getInstance().getExample(originUrl);
        for (ProjectFile file : storedExample.files) {
            if (!file.isModifiable() && project.readOnlyFileNames.contains(file.getName())) {
                project.files.add(file);
            }
        }
        return project;
    }


    private void sendResourceFile(HttpServletRequest request, HttpServletResponse response) {
        String path = request.getRequestURI() + "?" + request.getQueryString();
        path = ResponseUtils.substringAfterReturnAll(path, "resources");
        ErrorWriter.LOG_FOR_INFO.error(ErrorWriter.getInfoForLogWoIp(SessionInfo.TypeOfRequest.GET_RESOURCE.name(), "-1", "Resource doesn't downloaded from nginx: " + path));
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
            ResponseUtils.writeResponse(request, response, responseBody, errorCode);
        } catch (IOException e) {
            //This is an exception we can't send data to client
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, "UNKNOWN", request.getHeader("Origin"), "null");
        }
    }
}
