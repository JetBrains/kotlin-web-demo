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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.session.UserInfo;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.GoogleApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;

public class AuthorizationGoogleHelper extends AuthorizationHelper {
    private final String TYPE = "google";

    private static OAuthService googleService;

    private static Token requestToken;

    private static final String PROTECTED_RESOURCE_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
    private static final String SCOPE = "https://www.googleapis.com/auth/userinfo.profile";

    public String authorize() {
        try {
            googleService = new ServiceBuilder()
                    .provider(GoogleApi.class)
                    .apiKey("anonymous")
                    .apiSecret("anonymous")
                    .scope(SCOPE)
                    .callback("http://" + ApplicationSettings.AUTH_REDIRECT + ResponseUtils.generateRequestString("authorization", "google"))
                    .build();

            requestToken = googleService.getRequestToken();
            return googleService.getAuthorizationUrl(requestToken);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.AUTHORIZATION.name(), "unknown", "google");
        }
        return "";
    }

    @Override
    @Nullable
    public UserInfo verify(String url) {
        UserInfo userInfo = null;
        try {
            url = ResponseUtils.substringBetween(url, "oauth_verifier=", "&oauth_token=");
            Verifier verifier = new Verifier(url);
            Token accessToken = googleService.getAccessToken(requestToken, verifier);
            OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
            googleService.signRequest(accessToken, request);
            Response response = request.send();
            userInfo = new UserInfo();
            JsonNode object = new ObjectMapper().readTree(response.getBody()) ;
            userInfo.login(object.get("name").textValue(), object.get("id").textValue(), TYPE);

        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.AUTHORIZATION.name(), "unknown", "google: " + url);
        }
        return userInfo;
    }

}
