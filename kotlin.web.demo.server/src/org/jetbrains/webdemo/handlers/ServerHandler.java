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

package org.jetbrains.webdemo.handlers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.*;
import org.jetbrains.webdemo.authorization.AuthorizationFacebookHelper;
import org.jetbrains.webdemo.authorization.AuthorizationGoogleHelper;
import org.jetbrains.webdemo.authorization.AuthorizationHelper;
import org.jetbrains.webdemo.authorization.AuthorizationTwitterHelper;
import org.jetbrains.webdemo.database.MySqlConnector;
import org.jetbrains.webdemo.examplesLoader.ExamplesList;
import org.jetbrains.webdemo.help.HelpLoader;
import org.jetbrains.webdemo.log.LogDownloader;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.session.UserInfo;
import org.jetbrains.webdemo.sessions.HttpSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Calendar;

public class ServerHandler {


    public void handle(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        if (request.getQueryString() != null && request.getQueryString().equals("test")) {
            PrintWriter out = null;
            try {
                out = response.getWriter();
                out.write("ok");
            } catch (Throwable e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                        "TEST", request.getHeader("Origin"), "null");
            } finally {
                ServerResponseUtils.close(out);
            }
            return;
        }

        if (!ServerResponseUtils.isOriginAccepted(request)) {
            ErrorWriter.ERROR_WRITER.writeInfo(request.getHeader("Origin") + " try to connect to server");
            return;
        }

        SessionInfo sessionInfo;

        String param = request.getRequestURI() + "?" + request.getQueryString();
        try {
            RequestParameters parameters = RequestParameters.parseRequest(param);
            if (parameters.compareType("sendUserData")) {
                sessionInfo = setSessionInfo(request, parameters.getSessionId());
                MySqlConnector.getInstance().findUser(sessionInfo.getUserInfo());
                sendUserInformation(request, response, sessionInfo);
            } else if (parameters.compareType("getSessionId")) {
                sessionInfo = setSessionInfo(request, parameters.getSessionId());
                sendSessionId(request, response, sessionInfo, param);
            } else if (parameters.compareType("getUserName")) {
                sessionInfo = setSessionInfo(request, parameters.getSessionId());
                sendUserName(request, response, sessionInfo, param);
            } else if (parameters.compareType("authorization")) {
                sessionInfo = setSessionInfo(request, parameters.getSessionId());
                sendAuthorizationResult(request, response, parameters, sessionInfo);
            } else if (parameters.compareType("updateExamples")) {
                updateExamples(request, response);
            } else if (param.startsWith("/logs") || parameters.compareType("updateStatistics")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name());
                sessionInfo = setSessionInfo(request, parameters.getSessionId());
                sendListLogs(request, response, parameters.compareType("updateStatistics"), sessionInfo);
            } else if (parameters.compareType("showUserInfo")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name());
                sendUserInfoForStatistics(request, response);
            } else if (parameters.compareType("sortExceptions")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name());
                sendSortedExceptions(request, response, parameters);
            } else if (parameters.compareType("downloadLog")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name() + " " + param);
                sendLog(request, response, parameters);
            } else if (parameters.compareType("loadExample") && parameters.getArgs().equals("all")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_EXAMPLES_LIST.name());
                sendExamplesList(request, response);
            } else if (parameters.compareType("loadHelpForWords")) {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_HELP_FOR_WORDS.name());
                sendHelpContentForWords(request, response);
            } else if (parameters.compareType("highlight")
                    || parameters.compareType("complete")
                    || parameters.compareType("run")
                    || parameters.compareType("convertToKotlin")
                    || parameters.compareType("loadExample")
                    || parameters.compareType("saveFile")
                    || parameters.compareType("deleteFile")
                    || parameters.compareType("generatePublicLink")
                    || parameters.compareType("loadProject")
                    || parameters.compareType("addFile")
                    || parameters.compareType("writeLog")
                    || parameters.compareType("addProject")
                    || parameters.compareType("addExampleProject")
                    || parameters.compareType("deleteProject")){
                if (!parameters.compareType("writeLog")) {
                    sessionInfo = setSessionInfo(request, parameters.getSessionId());
                } else {
                    sessionInfo = new SessionInfo(request.getSession().getId());
                }
                if (!parameters.getSessionId().equals(sessionInfo.getId())) {
                    parameters.setSessionId(sessionInfo.getId());
                }
                HttpSession session = new HttpSession(sessionInfo, parameters);
                session.handle(request, response);
            } else {
                ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_RESOURCE.name() + " " + param);
                sendResourceFile(request, response);
            }
        } catch (Throwable e) {
            //Do not stop server
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    "UNKNOWN", "unknown", param);
            ServerResponseUtils.writeResponse(request, response, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendSessionId(HttpServletRequest request, HttpServletResponse response, SessionInfo sessionInfo, String param) {
        try {
            String id = sessionInfo.getId();
            ArrayNode array = new ArrayNode(JsonNodeFactory.instance);
            array.add(id);
            if (sessionInfo.getUserInfo().isLogin()) {
                array.add(URLEncoder.encode(sessionInfo.getUserInfo().getName(), "UTF-8"));
            }

            writeResponse(request, response, array.toString(), HttpServletResponse.SC_OK);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    "UNKNOWN", sessionInfo.getOriginUrl(), param);
        }
    }

    private void sendUserName(HttpServletRequest request, HttpServletResponse response, SessionInfo sessionInfo, String param) {
        try {
            ArrayNode array = new ArrayNode(JsonNodeFactory.instance);
            if (sessionInfo.getUserInfo().isLogin()) {
                array.add(URLEncoder.encode(sessionInfo.getUserInfo().getName(), "UTF-8"));
            } else {
                array.add("null");
            }
            writeResponse(request, response, array.toString(), HttpServletResponse.SC_OK);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    "UNKNOWN", sessionInfo.getOriginUrl(), param);
        }
    }

    private void sendAuthorizationResult(HttpServletRequest request, HttpServletResponse response, RequestParameters parameters, SessionInfo sessionInfo) {
        if (parameters.getArgs().equals("logout")) {
            sessionInfo.getUserInfo().logout();
            request.getSession().setAttribute("userInfo", sessionInfo.getUserInfo());
        } else {
            AuthorizationHelper helper;
            if (parameters.getArgs().contains("twitter")) {
                helper = new AuthorizationTwitterHelper();
            } else if (parameters.getArgs().contains("google")) {
                helper = new AuthorizationGoogleHelper();
            } else {
                helper = new AuthorizationFacebookHelper();
            }
            if (parameters.getArgs().contains("oauth_verifier") || parameters.getArgs().contains("code=")) {
                UserInfo info = helper.verify(parameters.getArgs());
                if (info != null) {

                    sessionInfo.setUserInfo(info);
                    MySqlConnector.getInstance().addNewUser(sessionInfo.getUserInfo());
                    request.getSession().setAttribute("userInfo", sessionInfo.getUserInfo());
                }
                try {
                    response.sendRedirect("http://" + ApplicationSettings.AUTH_REDIRECT);
                } catch (IOException e) {
                    ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                            "UNKNOWN", sessionInfo.getOriginUrl(), "cannot redirect to http://" + ApplicationSettings.AUTH_REDIRECT);
                }
            } else if (parameters.getArgs().contains("denied=")) {
                try {
                    response.sendRedirect("http://" + ApplicationSettings.AUTH_REDIRECT);
                } catch (IOException e) {
                    ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                            "UNKNOWN", sessionInfo.getOriginUrl(), "cannot redirect to http://" + ApplicationSettings.AUTH_REDIRECT);
                }
            } else {
                String verifyKey = helper.authorize();
                PrintWriter out = null;
                try {
                    out = response.getWriter();
                    out.write(verifyKey);
                } catch (Throwable e) {
                    ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                            "UNKNOWN", sessionInfo.getOriginUrl(), request.getRequestURI() + "/" + request.getQueryString());
                } finally {
                    ServerResponseUtils.close(out);
                }
            }
        }
    }

    private void sendUserInfoForStatistics(HttpServletRequest request, final HttpServletResponse response) {
        writeResponse(request, response, Statistics.getInstance().showMap(), HttpServletResponse.SC_OK);
    }


    private void updateExamples(HttpServletRequest request, final HttpServletResponse response) {
        String responseStr = ExamplesList.updateList();
        responseStr += HelpLoader.updateExamplesHelp();
        writeResponse(request, response, responseStr, HttpServletResponse.SC_OK);
    }

    private void sendSortedExceptions(final HttpServletRequest request, final HttpServletResponse response, RequestParameters parameters) {
        if (parameters.getArgs().contains("download")) {
            response.addHeader("Content-type", "application/x-download");
        }
        String from = ResponseUtils.substringBetween(parameters.getArgs(), "from=", "&to=");
        String to = ResponseUtils.substringAfter(parameters.getArgs(), "&to=");

        writeResponse(request, response, new LogDownloader().getSortedExceptions(from, to), 200);
    }

    @Nullable
    private SessionInfo setSessionInfo(final HttpServletRequest request, String sessionId) {
        if (request.getSession().isNew()) {
            Statistics.incNumberOfUsers();
        }
        SessionInfo sessionInfo = new SessionInfo(request.getSession().getId());
        UserInfo userInfo = (UserInfo) request.getSession().getAttribute("userInfo");

        if (userInfo == null) {
            userInfo = new UserInfo();
            request.getSession().setAttribute("userInfo", userInfo);
        }
        sessionInfo.setUserInfo(userInfo);
        sessionInfo.setOriginUrl(request.getHeader("Origin"));
        return sessionInfo;
    }

    private void sendHelpContentForWords(HttpServletRequest request, final HttpServletResponse response) {
        writeResponse(request, response, HelpLoader.getInstance().getHelpForWords(), 200);
    }

    private void sendLog(final HttpServletRequest request, final HttpServletResponse response, RequestParameters parameters) {
        String path;
        if (parameters.getArgs().contains("&download")) {
            response.addHeader("Content-type", "application/x-download");
        }
        if (parameters.getArgs().contains("&view")) {
            path = ResponseUtils.substringBefore(parameters.getArgs(), "&view");
        } else {
            path = ResponseUtils.substringBefore(parameters.getArgs(), "&download");
        }
        path = path.replaceAll("%5C", "/");
        writeResponse(request, response, new LogDownloader().download(path), 200);
    }

    private void sendListLogs(final HttpServletRequest request, final HttpServletResponse response, boolean updateStatistics, SessionInfo sessionInfo) {
        String responseStr = null;
        InputStream is = null;
        try {
            is = ServerHandler.class.getResourceAsStream("/logs.html");
            responseStr = ResponseUtils.readData(is, true);

        } catch (IOException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.GET_LOGS_LIST.name(), sessionInfo.getOriginUrl(), "Exception until downloading logs.html");
            writeResponse(request, response, "Cannot open this page", HttpServletResponse.SC_BAD_GATEWAY);
            return;
        } finally {
            ServerResponseUtils.close(is);
        }

        String links = new LogDownloader().getFilesLinks();
        responseStr = responseStr.replace("$LINKSTOLOGFILES$", links);
        responseStr = responseStr.replace("$CURRENTDATE$", ResponseUtils.getDate(Calendar.getInstance()));

        if (updateStatistics) {
            Statistics.getInstance().updateStatistics(true);
        }
        responseStr = Statistics.getInstance().writeStatistics(responseStr);
        writeResponse(request, response, responseStr, 200);
    }

    private void sendExamplesList(HttpServletRequest request, final HttpServletResponse response) {
        writeResponse(request, response, ExamplesList.getInstance().getListAsString(), HttpServletResponse.SC_OK);
    }

    private void sendUserInformation(final HttpServletRequest request, final HttpServletResponse response, SessionInfo info) {
        StringBuilder reqResponse = new StringBuilder();
        InputStream is = null;
        try {
            is = request.getInputStream();
            reqResponse.append(ResponseUtils.readData(is));
        } catch (IOException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.SEND_USER_DATA.name(), info.getOriginUrl(), info.getId());
            writeResponse(request, response, "Cannot read data from file", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        } finally {
            ServerResponseUtils.close(is);
        }
        try {
            reqResponse = new StringBuilder(URLDecoder.decode(reqResponse.toString(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.SEND_USER_DATA.name(), info.getOriginUrl(), info.getId());
        }
        ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLog(SessionInfo.TypeOfRequest.INC_NUMBER_OF_REQUESTS.name(), info.getId(), SessionInfo.TypeOfRequest.SEND_USER_DATA.name()));
        ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(SessionInfo.TypeOfRequest.SEND_USER_DATA.name(), info.getId(), ResponseUtils.substringAfter(reqResponse.toString(), "text=")));
        writeResponse(request, response, "OK", HttpServletResponse.SC_OK);
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

    //Send Response
    private void writeResponse(HttpServletRequest request, HttpServletResponse response, String responseBody, int errorCode) {
        try {
            ServerResponseUtils.writeResponse(request, response, responseBody, errorCode);
        } catch (IOException e) {
            //This is an exception we can't send data to client
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, "UNKNOWN", request.getHeader("Origin"), "null");
        }
    }
}

