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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.ApplicationSettings;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.session.UserInfo;
import org.scribe.builder.ServiceBuilder;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;

import java.util.concurrent.TimeUnit;

/**
 * Created by Semyon.Atamas on 4/22/2015.
 */
public class AuthorizationGithubHelper extends AuthorizationHelper {
    private static final String PROTECTED_RESOURCE_URL = "https://api.github.com/user";
    private static final Token EMPTY_TOKEN = null;
    private static OAuthService githubService;
    private final String TYPE = "github";

    public AuthorizationGithubHelper(String host) {
        super(host);
    }

    @Override
    public String getAuthorizationUrl() {
        try {
            githubService = new ServiceBuilder()
                    .provider(GithubApi.class)
                    .apiKey(ApplicationSettings.GITHUB_OAUTH_CREDENTIALS.KEY)
                    .apiSecret(ApplicationSettings.GITHUB_OAUTH_CREDENTIALS.SECRET)
                    .callback(getCallbackUrl())
                    .build();
            return githubService.getAuthorizationUrl(EMPTY_TOKEN);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.AUTHORIZATION.name(), "unknown", "github");
        }
        return "";
    }

    @Nullable
    @Override
    public UserInfo verify(String oauthVerifier) {
        UserInfo userInfo = null;
        try {
            Verifier verifier = new Verifier(oauthVerifier);
            Token accessToken = githubService.getAccessToken(EMPTY_TOKEN, verifier);
            OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
            request.setConnectTimeout(TIMEOUT, TimeUnit.MILLISECONDS);
            githubService.signRequest(accessToken, request);
            Response response = request.send();

            JsonNode object = new ObjectMapper().readTree(response.getBody());
            String name = object.get("name").textValue() != null ? object.get("name").textValue() : "Anonymous";
            userInfo = new UserInfo();
            userInfo.login(name, object.get("id").asText(), TYPE);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.AUTHORIZATION.name(), "unknown", "github: " + oauthVerifier);
        }
        return userInfo;
    }

    @NotNull
    @Override
    protected String getType() {
        return "github";
    }
}
