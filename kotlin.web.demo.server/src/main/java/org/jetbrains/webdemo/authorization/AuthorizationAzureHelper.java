/*
 * Copyright 2000-2015 JetBrains s.r.o.
 * Copyright 2019 Franz-Josef Färber
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
import com.github.scribejava.apis.MicrosoftAzureActiveDirectoryApi;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.*;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.ApplicationSettings;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.session.UserInfo;


public class AuthorizationAzureHelper extends AuthorizationHelper {

    private static final String PROTECTED_RESOURCE_URL = "https://graph.windows.net/me?api-version=1.6&$select=userPrincipalName,immutableId";
    private static final String TYPE = "azure";

    private static OAuth20Service azureService;

    public AuthorizationAzureHelper(String host) {
        super(host);
    }

    @Override
    public String getAuthorizationUrl() {
        try {
            azureService = new ServiceBuilder(ApplicationSettings.AZURE_OAUTH_CREDENTIALS.KEY)
                    .apiSecret(ApplicationSettings.AZURE_OAUTH_CREDENTIALS.SECRET)
                    .defaultScope("https://graph.microsoft.com/User.Read")
                    .callback(getCallbackUrl())
                    .build(MicrosoftAzureActiveDirectoryApi.custom(ApplicationSettings.AZURE_OAUTH_CREDENTIALS.TENANT, null));
            return azureService.getAuthorizationUrl();
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.AUTHORIZATION.name(), "unknown", TYPE);
            throw new RuntimeException(e);
        }
    }

    @Nullable
    @Override
    public UserInfo verify(String oauthVerifier) {
        try {
            final OAuth2AccessToken accessToken = azureService.getAccessToken(oauthVerifier);
            final OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
            azureService.signRequest(accessToken, request);

            final Response response = azureService.execute(request);
            final JsonNode object = new ObjectMapper().readTree(response.getBody());

            final UserInfo userInfo = new UserInfo();
            userInfo.login(object.get("userPrincipalName").asText(), object.get("immutableId").asText(), TYPE);
            return userInfo;
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.AUTHORIZATION.name(), "unknown", "azure: " + oauthVerifier);
            throw new RuntimeException(e);
        }
    }

    @NotNull
    @Override
    protected String getType() {
        return TYPE;
    }
}
