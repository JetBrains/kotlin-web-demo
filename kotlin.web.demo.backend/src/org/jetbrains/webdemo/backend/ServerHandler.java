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

import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ErrorWriterOnServer;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.Statistics;
import org.jetbrains.webdemo.handlers.ServerResponseUtils;
import org.jetbrains.webdemo.log.LogDownloader;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.session.UserInfo;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Map;

public class ServerHandler {


    public void handle(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        if (request.getQueryString() != null && request.getQueryString().equals("test")) {
            response.setStatus(HealthChecker.getInstance().getStatus());
        } else if (!ServerResponseUtils.isOriginAccepted(request)) {
            ErrorWriter.ERROR_WRITER.writeInfo(request.getHeader("Origin") + " try to connect to server");
        } else {
            SessionInfo sessionInfo;

            String param = request.getRequestURI() + "?" + request.getQueryString();
            try {
                Map<String, String[]> parameters = request.getParameterMap();
                switch (parameters.get("type")[0]) {
                    case ("updateStatistics"):
                        ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.GET_LOGS_LIST.name());
                        sessionInfo = setSessionInfo(request.getSession(), request.getHeader("Origin"));
                        sendListLogs(request, response, parameters.get("type")[0].equals("updateStatistics"), sessionInfo);
                        break;
                    case ("sortExceptions"):
                        ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name());
                        sendSortedExceptions(request, response, parameters);
                        break;
                    case ("downloadLog"):
                        ErrorWriterOnServer.LOG_FOR_INFO.info(SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name() + " " + param);
                        sendLog(request, response, parameters);
                        break;
                    default: {
                        if (!parameters.get("type")[0].equals("writeLog")) {
                            sessionInfo = setSessionInfo(request.getSession(), request.getHeader("Origin"));
                        } else {
                            sessionInfo = new SessionInfo(request.getSession().getId());
                        }
                        MyHttpSession session = new MyHttpSession(sessionInfo, parameters);
                        session.handle(request, response);
                    }
                }
            } catch (Throwable e) {
                //Do not stop server
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                        "UNKNOWN", "unknown", param);
                ServerResponseUtils.writeResponse(request, response, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    private void sendSortedExceptions(final HttpServletRequest request, final HttpServletResponse response, Map<String, String[]> parameters) {
        if (parameters.get("args")[0].contains("download")) {
            response.addHeader("Content-type", "application/x-download");
        }
        String from = ResponseUtils.substringBetween(parameters.get("args")[0], "from=", "&to=");
        String to = ResponseUtils.substringAfter(parameters.get("args")[0], "&to=");

        writeResponse(request, response, new LogDownloader().getSortedExceptions(from, to), 200);
    }

    @Nullable
    private SessionInfo setSessionInfo(final HttpSession session, String originUrl) {
        if (session.isNew()) {
            Statistics.incNumberOfUsers();
        }
        SessionInfo sessionInfo = new SessionInfo(session.getId());
        UserInfo userInfo = (UserInfo) session.getAttribute("userInfo");

        if (userInfo == null) {
            userInfo = new UserInfo();
            session.setAttribute("userInfo", userInfo);
        }
        sessionInfo.setUserInfo(userInfo);
        sessionInfo.setOriginUrl(originUrl);
        return sessionInfo;
    }

    private void sendLog(final HttpServletRequest request, final HttpServletResponse response, Map<String, String[]> parameters) {
        String path;
        if (parameters.get("args")[0].contains("&download")) {
            response.addHeader("Content-type", "application/x-download");
        }
        if (parameters.get("args")[0].contains("&view")) {
            path = ResponseUtils.substringBefore(parameters.get("args")[0], "&view");
        } else {
            path = ResponseUtils.substringBefore(parameters.get("args")[0], "&download");
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

