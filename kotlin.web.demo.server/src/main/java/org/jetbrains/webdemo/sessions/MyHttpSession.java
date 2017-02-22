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
import org.jetbrains.webdemo.*;
import org.jetbrains.webdemo.database.DatabaseOperationException;
import org.jetbrains.webdemo.database.MySqlConnector;
import org.jetbrains.webdemo.examples.ExamplesFolder;
import org.jetbrains.webdemo.examples.ExamplesUtils;
import org.jetbrains.webdemo.handlers.ServerHandler;
import org.jetbrains.webdemo.session.SessionInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
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
                case ("getUserName"):
                    sendUserName(request, response, sessionInfo);
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
                case ("addAdventOfCodeProject"):
                    sendAddAdventOfCodeProjectResult();
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
                case ("loadProjectName"):
                    sendLoadProjectNameResult();
                    break;
                case ("checkIfProjectExists"):
                    sendExistenceCheckResult();
                    break;
                case ("saveSolution"):
                    sendSaveSolutionResult();
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

    private void sendUserName(HttpServletRequest request, HttpServletResponse response, SessionInfo sessionInfo) {
        try {
            ObjectNode responseBody = new ObjectNode(JsonNodeFactory.instance);
            if (sessionInfo.getUserInfo().isLogin()) {
                responseBody.put("isLoggedIn", true);
                responseBody.put("userName", URLEncoder.encode(sessionInfo.getUserInfo().getName(), "UTF-8"));
                responseBody.put("type", sessionInfo.getUserInfo().getType());
            } else {
                responseBody.put("isLoggedIn", false);
            }
            writeResponse(responseBody.toString(), HttpServletResponse.SC_OK);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    "UNKNOWN", sessionInfo.getOriginUrl(), request.getRequestURI() + "?" + request.getQueryString());
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

    private void sendSaveSolutionResult() {
        try {
            boolean completed = Boolean.parseBoolean(request.getParameter("completed"));
            Project solution = objectMapper.readValue(request.getParameter("solution"), Project.class);
            solution.args = "";
            MySqlConnector.getInstance().saveSolution(sessionInfo.getUserInfo(), solution, completed);
        } catch (IOException e) {
            writeResponse("Can't write response", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
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

    private void sendLoadProjectNameResult() {
        try {
            String project_id = request.getParameter("project_id");
            String response = MySqlConnector.getInstance().getProjectNameById(project_id);
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
                    ExamplesUtils.addHiddenFilesToProject(currentProject);
                    String publicId = MySqlConnector.getInstance().addProject(sessionInfo.getUserInfo(), currentProject, "USER_PROJECT");
                    ObjectNode result = new ObjectNode(JsonNodeFactory.instance);
                    result.put("publicId", publicId);
                    Project project = MySqlConnector.getInstance().getProjectContent(publicId);
                    result.put("content", objectMapper.writeValueAsString(project));
                    writeResponse(result.toString(), HttpServletResponse.SC_OK);
                } catch (IOException e) {
                    writeResponse("Can't parse file", HttpServletResponse.SC_BAD_REQUEST);
                }
            } else {
                String name = request.getParameter("name");
                String publicIds = MySqlConnector.getInstance().addProject(sessionInfo.getUserInfo(), name, "USER_PROJECT");
                writeResponse(publicIds, HttpServletResponse.SC_OK);
            }
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
        }
    }

    private void sendAddAdventOfCodeProjectResult() {
        try {
            String inputFileContent = request.getParameter("inputFileContent");
            String name = request.getParameter("name");
            String publicIds = MySqlConnector.getInstance().addAdventOfCodeProject(sessionInfo.getUserInfo(), name, inputFileContent);
            writeResponse(publicIds, HttpServletResponse.SC_OK);
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
            Map<String, Boolean> taskStatuses = MySqlConnector.getInstance().getUserTaskStatuses(sessionInfo.getUserInfo());
            for (ExamplesFolder folder : ExamplesFolder.ROOT_FOLDER.getChildFolders()) {
                addFolderContent(responseBody, folder, taskStatuses);
            }

            ObjectNode adventOfCodeContent = responseBody.addObject();
            adventOfCodeContent.put("name", "Advent of Code");
            adventOfCodeContent.put("id", "advent%20of%20code");
            adventOfCodeContent.putArray("childFolders");
            if (sessionInfo.getUserInfo().isLogin()) {
                adventOfCodeContent.put("projects", MySqlConnector.getInstance().getProjectHeaders(sessionInfo.getUserInfo(), "ADVENT_OF_CODE_PROJECT"));
            } else {
                adventOfCodeContent.putArray("projects");
            }

            ObjectNode myProgramsContent = responseBody.addObject();
            myProgramsContent.put("name", "My programs");
            myProgramsContent.put("id", "My%20programs");
            myProgramsContent.putArray("childFolders");
            if (sessionInfo.getUserInfo().isLogin()) {
                myProgramsContent.put("projects", MySqlConnector.getInstance().getProjectHeaders(sessionInfo.getUserInfo(), "USER_PROJECT"));
            } else {
                myProgramsContent.putArray("projects");
            }


            writeResponse(responseBody.toString(), HttpServletResponse.SC_OK);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }


    private void addFolderContent(ArrayNode arrayNode, ExamplesFolder folder, Map<String, Boolean> taskStatuses) {
        ObjectNode folderContent = arrayNode.addObject();
        folderContent.put("name", folder.getName());
        folderContent.put("id", folder.getId());
        if (folder.isTaskFolder()) folderContent.put("levels", objectMapper.valueToTree(folder.getLevels()));
        folderContent.put("isTaskFolder", folder.isTaskFolder());
        ArrayNode exampleHeaders = folderContent.putArray("projects");
        for (Project example : folder.getExamples()) {
            ObjectNode exampleHeader = exampleHeaders.addObject();
            exampleHeader.put("name", example.name);
            exampleHeader.put("publicId", example.id);
            if (taskStatuses.keySet().contains(example.id)) {
                exampleHeader.put("completed", taskStatuses.get(example.id));
                exampleHeader.put("modified", true);
            } else {
                exampleHeader.put("modified", false);
            }
        }
        ArrayNode childFolders = folderContent.putArray("childFolders");

        for (ExamplesFolder childFolder : folder.getChildFolders()) {
            addFolderContent(childFolders, childFolder, taskStatuses);
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
            String id = request.getParameter("publicId");
            Project result = MySqlConnector.getInstance().getProjectContent(id);
            if (result != null) {
                if(!KotlinVersionsManager.INSTANCE.getKotlinVersions().contains(result.getCompilerVersion())){
                    result.setCompilerVersion(KotlinVersionsManager.INSTANCE.getLatestStableVersion());
                }
                writeResponse(objectMapper.writeValueAsString(result), HttpServletResponse.SC_OK);
            } else {
                writeResponse("", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        } catch (IOException e) {
            writeResponse("Can't serialize project", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private void sendSaveProjectResult() {
        try {
            currentProject = objectMapper.readValue(request.getParameter("project"), Project.class);
            String publicId = request.getParameter("publicId");
            String type = request.getParameter("projectType");
            MySqlConnector.getInstance().saveProject(sessionInfo.getUserInfo(), publicId, currentProject, type);
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
            String id = request.getParameter("publicId");
            boolean clearCache = Boolean.parseBoolean(request.getParameter("ignoreCache"));

            if (sessionInfo.getUserInfo().isLogin() && clearCache) {
                MySqlConnector.getInstance().deleteSolution(sessionInfo.getUserInfo(), id);
            }

            Project example = sessionInfo.getUserInfo().isLogin() ?
                    ExamplesUtils.getUserVersionOfExample(sessionInfo.getUserInfo(), id) :
                    ExamplesUtils.getExample(id);
            writeResponse(objectMapper.writeValueAsString(example), HttpServletResponse.SC_OK);
        } catch (IOException e) {
            writeResponse("Can't write response", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (NullPointerException e) {
            writeResponse("Can't find example", HttpServletResponse.SC_BAD_REQUEST);
        } catch (DatabaseOperationException e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_FORBIDDEN);
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
            response.addHeader("Cache-Control", "no-cache");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(statusCode);
            if(!responseBody.equals("")) {
                try (PrintWriter writer = response.getWriter()) {
                    writer.write(responseBody);
                }
            }
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

}
