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

package org.jetbrains.webdemo.servlet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jetbrains.webdemo.CommandRunner;
import org.jetbrains.webdemo.authorization.AuthorizationHelper;
import org.jetbrains.webdemo.database.DatabaseOperationException;
import org.jetbrains.webdemo.database.MySqlConnector;
import org.jetbrains.webdemo.session.UserInfo;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthorizationServlet extends HttpServlet {
    private static Log log = LogFactory.getLog(AuthorizationServlet.class);

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) {
        try {
            String contextPath = request.getRequestURI();
            if (contextPath.startsWith("/login")) {
                processAuthorizeRequest(request, response);
            } else if (contextPath.startsWith("/verify")) {
                processVerifyRequest(request, response);
            } else if (contextPath.startsWith("/logout")) {
                processLogoutRequest(request, response);
            }
        } catch (Throwable e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("Unknown authorization exception.", e);
        }
    }

    @Override
    public void init() {
        try {
            InitialContext initialContext = new InitialContext();
            Context envCtx = (Context) initialContext.lookup("java:comp/env");
            CommandRunner.setServerSettingFromTomcatConfig("azure_key", (String) envCtx.lookup("azure_key"));
            CommandRunner.setServerSettingFromTomcatConfig("azure_secret", (String) envCtx.lookup("azure_secret"));
            CommandRunner.setServerSettingFromTomcatConfig("azure_tenant", (String) envCtx.lookup("azure_tenant"));
            CommandRunner.setServerSettingFromTomcatConfig("google_key", (String) envCtx.lookup("google_key"));
            CommandRunner.setServerSettingFromTomcatConfig("google_secret", (String) envCtx.lookup("google_secret"));
            CommandRunner.setServerSettingFromTomcatConfig("twitter_key", (String) envCtx.lookup("twitter_key"));
            CommandRunner.setServerSettingFromTomcatConfig("twitter_secret", (String) envCtx.lookup("twitter_secret"));
            CommandRunner.setServerSettingFromTomcatConfig("facebook_key", (String) envCtx.lookup("facebook_key"));
            CommandRunner.setServerSettingFromTomcatConfig("facebook_secret", (String) envCtx.lookup("facebook_secret"));
            CommandRunner.setServerSettingFromTomcatConfig("github_key", (String) envCtx.lookup("github_key"));
            CommandRunner.setServerSettingFromTomcatConfig("github_secret", (String) envCtx.lookup("github_secret"));
            CommandRunner.setServerSettingFromTomcatConfig("jba_secret", (String) envCtx.lookup("jba_secret"));
        } catch (Throwable e) {
            log.fatal("Can't initialize authorization servlet", e);
        }
    }

    private void processAuthorizeRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        AuthorizationHelper helper = AuthorizationHelper.getHelper(getType(request), getHost(request));
        response.sendRedirect(helper.getAuthorizationUrl());
    }

    private void processVerifyRequest(HttpServletRequest request, HttpServletResponse response) throws IOException, DatabaseOperationException {
        try {
            AuthorizationHelper helper = AuthorizationHelper.getHelper(getType(request), getHost(request));
            UserInfo userInfo = null;

            if (request.getParameter("oauth_verifier") != null) {
                userInfo = helper.verify(request.getParameter("oauth_verifier"));
            } else if (request.getParameter("code") != null) {
                userInfo = helper.verify(request.getParameter("code"));
            } else if (request.getParameter("jwt") != null) {
                userInfo = helper.verify(request.getParameter("jwt"));
            }

            if(userInfo != null && !userInfo.getType().equals("")) {
                MySqlConnector.getInstance().addNewUser(userInfo);
                request.getSession().setAttribute("userInfo", userInfo);
            }
        } finally {
            response.sendRedirect("http://" + getHost(request));
        }
    }

    private void processLogoutRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession();
        if (session != null) {
            session.invalidate();
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private String getType(HttpServletRequest request) {
        String contextPath = request.getRequestURI();
        return contextPath.split("/")[2];
    }

    private String getHost(HttpServletRequest request) {
        return request.getHeader("Host") != null ?
                request.getHeader("Host") :
                request.getServerName() + ":" + request.getServerPort();
    }
}
