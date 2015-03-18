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

package org.jetbrains.webdemo.handlers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.ApplicationSettings;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.authorization.AuthorizationFacebookHelper;
import org.jetbrains.webdemo.authorization.AuthorizationGoogleHelper;
import org.jetbrains.webdemo.authorization.AuthorizationHelper;
import org.jetbrains.webdemo.authorization.AuthorizationTwitterHelper;
import org.jetbrains.webdemo.database.MySqlConnector;
import org.jetbrains.webdemo.help.HelpLoader;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.session.UserInfo;
import org.jetbrains.webdemo.sessions.MyHttpSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;

public class ServerHandler {


    public void handle(final HttpServletRequest request, final HttpServletResponse response) throws IOException {

        if (request.getQueryString() != null && request.getQueryString().equals("test")) {
            try (PrintWriter out = response.getWriter()) {
                out.write("ok");
            } catch (Throwable e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                        "TEST", request.getHeader("Origin"), "null");
            }
        } else if (!ResponseUtils.isOriginAccepted(request)) {
            ErrorWriter.ERROR_WRITER.writeInfo(request.getHeader("Origin") + " try to connect to server");
        } else {
            SessionInfo sessionInfo;
            try {
                switch (request.getParameter("type")) {
                    case ("getSessionId"):
                        sessionInfo = setSessionInfo(request.getSession(), request.getHeader("Origin"));
                        sendSessionId(request, response, sessionInfo);
                        break;
                    case ("getUserName"):
                        sessionInfo = setSessionInfo(request.getSession(), request.getHeader("Origin"));
                        sendUserName(request, response, sessionInfo);
                        break;
                    case ("authorization"):
                        sessionInfo = setSessionInfo(request.getSession(), request.getHeader("Origin"));
                        sendAuthorizationResult(request, response, sessionInfo);
                        break;
                    case ("loadHelpForWords"):
                        sendHelpContentForWords(request, response);
                        break;
                    case ("logout"): {
                        HttpSession session = request.getSession();
                        if (session != null) {
                            session.invalidate();
                        }
                        writeResponse(request, response, "ok", HttpServletResponse.SC_OK);
                        break;
                    }
                    case ("google_key"): {
                        writeResponse(request, response, ApplicationSettings.GOOGLE_OAUTH_CREDENTIALS.KEY, HttpServletResponse.SC_OK);
                        break;
                    }
                    default: {
                        sessionInfo = setSessionInfo(request.getSession(), request.getHeader("Origin"));
                        MyHttpSession session = new MyHttpSession(sessionInfo);
                        session.handle(request, response);
                    }
                }
            } catch (Throwable e) {
                //Do not stop server
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                        "UNKNOWN", "unknown", request.getRequestURI() + "?" + request.getQueryString());
                ResponseUtils.writeResponse(request, response, "Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }
        }
    }

    private void sendSessionId(HttpServletRequest request, HttpServletResponse response, SessionInfo sessionInfo) {
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
                    "UNKNOWN", sessionInfo.getOriginUrl(), request.getRequestURI() + "?" + request.getQueryString());
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
            writeResponse(request, response, responseBody.toString(), HttpServletResponse.SC_OK);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    "UNKNOWN", sessionInfo.getOriginUrl(), request.getRequestURI() + "?" + request.getQueryString());
        }
    }

    private void sendAuthorizationResult(HttpServletRequest request, HttpServletResponse response, SessionInfo sessionInfo) {
        if (request.getParameter("args").equals("logout")) {
            sessionInfo.getUserInfo().logout();
            request.getSession().setAttribute("userInfo", sessionInfo.getUserInfo());
        } else {
            AuthorizationHelper helper;
            if (request.getParameter("args").equals("twitter")) {
                helper = new AuthorizationTwitterHelper();
            } else if (request.getParameter("args").equals("google")) {
                helper = new AuthorizationGoogleHelper();
            } else {
                helper = new AuthorizationFacebookHelper();
            }
            if (request.getParameter("oauth_verifier") != null ||
                    request.getParameter("code") != null) {
                try {
                    UserInfo info;
                    if (request.getParameter("oauth_verifier") != null) {
                        info = helper.verify(request.getParameter("oauth_verifier"));
                    } else {
                        info = helper.verify(request.getParameter("code"));
                    }
                    if (info != null) {
                        sessionInfo.setUserInfo(info);
                        MySqlConnector.getInstance().addNewUser(sessionInfo.getUserInfo());
                        request.getSession().setAttribute("userInfo", sessionInfo.getUserInfo());
                    }
                } catch (Throwable e) {
                    ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                            "UNKNOWN", sessionInfo.getOriginUrl(), "Can't authorize user " + request.getQueryString());
                } finally {
                    try {
                        response.sendRedirect("http://" + ApplicationSettings.AUTH_REDIRECT);
                    } catch (IOException e) {
                        ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                                "UNKNOWN", sessionInfo.getOriginUrl(), "cannot redirect to http://" + ApplicationSettings.AUTH_REDIRECT);
                    }
                }
            } else if (request.getParameter("denied") != null) {
                try {
                    response.sendRedirect("http://" + ApplicationSettings.AUTH_REDIRECT);
                } catch (IOException e) {
                    ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                            "UNKNOWN", sessionInfo.getOriginUrl(), "cannot redirect to http://" + ApplicationSettings.AUTH_REDIRECT);
                }
            } else {
                String verifyKey = helper.authorize();
                try (PrintWriter out = response.getWriter()) {
                    out.write(verifyKey);
                } catch (Throwable e) {
                    ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                            "UNKNOWN", sessionInfo.getOriginUrl(), request.getRequestURI() + "/" + request.getQueryString());
                }
            }
        }
    }

    @Nullable
    private SessionInfo setSessionInfo(final HttpSession session, String originUrl) {
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

    private void sendHelpContentForWords(HttpServletRequest request, final HttpServletResponse response) {
        writeResponse(request, response, HelpLoader.getInstance().getHelpForWords(), 200);
    }


    //Send Response
    private void writeResponse(HttpServletRequest request, HttpServletResponse response, String responseBody, int errorCode) {
        try {
            ResponseUtils.writeResponse(request, response, responseBody, errorCode);
        } catch (IOException e) {
            //This is an exception we can't send data to client
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, "UNKNOWN", request.getHeader("Origin"), "null");
        }
    }
}

