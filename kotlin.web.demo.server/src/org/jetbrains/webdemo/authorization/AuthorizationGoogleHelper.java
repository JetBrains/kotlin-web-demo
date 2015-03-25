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

package org.jetbrains.webdemo.authorization;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.ApplicationSettings;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.session.UserInfo;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;

import java.util.concurrent.TimeUnit;

public class AuthorizationGoogleHelper extends AuthorizationHelper {
    private static final String PROTECTED_RESOURCE_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
    private static final String SCOPE = "https://www.googleapis.com/auth/userinfo.profile";
    private static final Token EMPTY_TOKEN = null;
    private static OAuthService googleService;
    private final String TYPE = "google";

    public AuthorizationGoogleHelper(String host) {
        super(host);
    }

    public String getAuthorizationUrl() {
        try {
            googleService = new ServiceBuilder()
                    .provider(Google2Api.class)
                    .apiKey(ApplicationSettings.GOOGLE_OAUTH_CREDENTIALS.KEY)
                    .apiSecret(ApplicationSettings.GOOGLE_OAUTH_CREDENTIALS.SECRET)
                    .scope(SCOPE)
                    .callback("http://" + host + ResponseUtils.generateRequestString("authorization", "google"))
                    .build();

            return googleService.getAuthorizationUrl(EMPTY_TOKEN);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.AUTHORIZATION.name(), "unknown", "google");
        }
        return "";
    }

    @Override
    @Nullable
    public UserInfo verify(String oauthVerifier) {
        UserInfo userInfo = null;
        try {
            Verifier verifier = new Verifier(oauthVerifier);
            Token accessToken = googleService.getAccessToken(EMPTY_TOKEN, verifier);
            OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
            request.setConnectTimeout(TIMEOUT, TimeUnit.MILLISECONDS);
            googleService.signRequest(accessToken, request);
            Response response = request.send();
            userInfo = new UserInfo();
            JsonNode object = new ObjectMapper().readTree(response.getBody()) ;
            String firstName = object.has("given_name") ? object.get("given_name").asText() : "";
            String lastName = object.has("family_name") ? object.get("family_name").asText() : "";
            String id = object.get("id").textValue();

            userInfo.login(firstName + " " + lastName, id, TYPE);

        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.AUTHORIZATION.name(), "unknown", "google: " + oauthVerifier);
        }
        return userInfo;
    }

}
