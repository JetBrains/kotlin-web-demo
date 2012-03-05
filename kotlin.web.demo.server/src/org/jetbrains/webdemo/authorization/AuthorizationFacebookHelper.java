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
import org.jetbrains.webdemo.server.ServerSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.session.UserInfo;
import org.json.JSONObject;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.FacebookApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 12/30/11
 * Time: 3:54 PM
 */

public class AuthorizationFacebookHelper extends AuthorizationHelper {
    private final String TYPE = "facebook";
    
    private static OAuthService facebookService;
    private static final String PROTECTED_RESOURCE_URL = "https://graph.facebook.com/me";
    private static final Token EMPTY_TOKEN = null;

    public String authorize() {
        try {
            facebookService = new ServiceBuilder()
                    .provider(FacebookApi.class)
                    .apiKey("281097941954775")
                    .apiSecret("c834e3f743a0ea79d8d289b252b9bdb3")
                    .callback("http://" + ServerSettings.AUTH_REDIRECT + ResponseUtils.generateRequestString("authorization", "facebook"))
                    .build();
            return facebookService.getAuthorizationUrl(EMPTY_TOKEN);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.AUTHORIZATION.name(), "facebook");
        }
        return "";
    }

    @Override
    @Nullable
    public UserInfo verify(String url) {
        UserInfo userInfo = null;
        try {
            String code = ResponseUtils.substringAfter(url, "code=");
            Verifier verifier = new Verifier(code);
            Token accessToken = facebookService.getAccessToken(EMPTY_TOKEN, verifier);
            OAuthRequest request = new OAuthRequest(Verb.GET, PROTECTED_RESOURCE_URL);
            facebookService.signRequest(accessToken, request);
            Response response = request.send();

            JSONObject object = new JSONObject(response.getBody());
            userInfo = new UserInfo();
            userInfo.login((String) object.get("name"), (String) object.get("id"), TYPE);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.AUTHORIZATION.name(), "facebook: " + url);
        }
        return userInfo;
    }

}
