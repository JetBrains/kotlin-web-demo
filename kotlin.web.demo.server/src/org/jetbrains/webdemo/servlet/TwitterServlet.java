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

import org.jetbrains.webdemo.ApplicationSettings;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.examples.Example;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.net.URLDecoder;

public class TwitterServlet extends HttpServlet {
    private String apiKey;
    private String apiSecret;

    @Override
    public void init() {
        try {
            InitialContext initialContext = new InitialContext();
            Context envContext = (Context) initialContext.lookup("java:comp/env");
            apiKey = (String) envContext.lookup("twitter_rw_key");
            apiSecret = (String) envContext.lookup("twitter_rw_secret");
        } catch (Exception e) {
            ErrorWriter.writeExceptionToConsole(e);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        switch (request.getPathInfo()) {
            case "/login":
                login(request, response);
                break;
            case "/tweet":
                tweet(request, response);
                break;
        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            Twitter twitter = new TwitterFactory().getInstance();
            twitter.setOAuthConsumer(apiKey, apiSecret);
            request.getSession().setAttribute("twitter", twitter);
            String callbackURL =
                    request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/twitter/tweet";
            request.getSession().setAttribute("tweetText", request.getParameter("tweet-text"));
            request.getSession().setAttribute("kotlin-level", request.getParameter("kotlin-level"));
            RequestToken requestToken = twitter.getOAuthRequestToken(callbackURL);
            request.getSession().setAttribute("requestToken", requestToken);
            response.sendRedirect(requestToken.getAuthenticationURL());
        } catch (Throwable e) {
            throw new ServletException(e);
        }
    }

    private void tweet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        try {
            Twitter twitter = (Twitter) request.getSession().getAttribute("twitter");
            RequestToken requestToken = (RequestToken) request.getSession().getAttribute("requestToken");
            String verifier = request.getParameter("oauth_verifier");
            twitter.getOAuthAccessToken(requestToken, verifier);

            String status = (String) request.getSession().getAttribute("tweetText");
            request.getSession().removeAttribute("tweetText");
            StatusUpdate statusUpdate = new StatusUpdate(status);

            String level = (String) request.getSession().getAttribute("kotlin-level");
            request.getSession().removeAttribute("level");
            statusUpdate.setMedia(new File(ApplicationSettings.WEBAPP_ROOT_DIRECTORY + "static/images/" + level + "level.gif"));

            twitter.updateStatus(statusUpdate);
            request.getSession().removeAttribute("requestToken");
            response.sendRedirect(request.getContextPath() + "/");
        } catch (Throwable e) {
            throw new ServletException(e);
        }
    }
}
