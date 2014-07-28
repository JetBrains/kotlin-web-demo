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

package org.jetbrains.webdemo.authorization;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.session.UserInfo;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class AuthorizationTwitterHelper extends AuthorizationHelper {
    private final String TYPE = "twitter";

    private static OAuthService twitterService;
    private static Token requestToken;
    private static final String PROTECTED_RESOURCE_URL = "http://api.twitter.com/1.1/account/verify_credentials.json";

    public String authorize() {
        try {
            twitterService = new ServiceBuilder()
                    .provider(TwitterApi.class)
                    .apiKey("g0dAeSZpnxTHxRKV2UZFGg")
                    .apiSecret("NSfUf8o3BhyT96U6hcCarWIUEwz6Le4FY6Em7WBPtuw")
                    .callback("http://" + ApplicationSettings.AUTH_REDIRECT + ResponseUtils.generateRequestString("authorization", "twitter"))
                    .build();
            requestToken = twitterService.getRequestToken();
            return twitterService.getAuthorizationUrl(requestToken);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.AUTHORIZATION.name(), "unknown", "twitter");
        }
        return "";
    }

    @Override
    @Nullable
    public UserInfo verify(String url) {
        UserInfo userInfo = null;
        try {
            String authUrl = ResponseUtils.substringAfter(url, "oauth_verifier=");
            Verifier verifier = new Verifier(authUrl);
            Token accessToken = twitterService.getAccessToken(requestToken, verifier);
            OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
            twitterService.signRequest(accessToken, request); // the access token from step 4
            Response response = request.send();

            JSONObject obj = new JSONObject(response.getBody());
            userInfo = new UserInfo();
            String id = obj.get("id").toString();
            String name = obj.has("name") ? obj.get("name").toString() : "";

            if (name != null && id != null) {
                userInfo.login(name, id, TYPE);
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.AUTHORIZATION.name(), "unknown", "twitter: " + url);
        }
        return userInfo;
    }
}
