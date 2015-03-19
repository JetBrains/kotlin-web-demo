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
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;

import java.util.concurrent.TimeUnit;

public class AuthorizationFacebookHelper extends AuthorizationHelper {
    private static final String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/me";
    private static final Token EMPTY_TOKEN = null;
    private static OAuthService facebookService;
    private final String TYPE = "facebook";

    public AuthorizationFacebookHelper(String host) {
        super(host);
    }

    public String authorize() {
        try {
            facebookService = new ServiceBuilder()
                    .provider(FacebookApi.class)
                    .apiKey(ApplicationSettings.FACEBOOK_OAUTH_CREDENTIALS.KEY)
                    .apiSecret(ApplicationSettings.FACEBOOK_OAUTH_CREDENTIALS.SECRET)
                    .callback("http://" + host + ResponseUtils.generateRequestString("authorization", "facebook"))
                    .build();
            return facebookService.getAuthorizationUrl(EMPTY_TOKEN);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.AUTHORIZATION.name(), "unknown", "facebook");
        }
        return "";
    }

    @Override
    @Nullable
    public UserInfo verify(String oauthVerifier) {
        UserInfo userInfo = null;
        try {
            Verifier verifier = new Verifier(oauthVerifier);
            Token accessToken = facebookService.getAccessToken(EMPTY_TOKEN, verifier);
            OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
            request.setConnectTimeout(TIMEOUT, TimeUnit.MILLISECONDS);
            facebookService.signRequest(accessToken, request);
            Response response = request.send();

            JsonNode object = new ObjectMapper().readTree(response.getBody());
            userInfo = new UserInfo();
            userInfo.login(object.get("name").textValue(), object.get("id").asText(), TYPE);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.AUTHORIZATION.name(), "unknown", "facebook: " + oauthVerifier);
        }
        return userInfo;
    }

}
