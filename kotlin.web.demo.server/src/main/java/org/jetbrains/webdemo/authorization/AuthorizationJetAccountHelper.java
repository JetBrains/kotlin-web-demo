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

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.crypto.MACVerifier;
import net.minidev.json.JSONObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.ApplicationSettings;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.session.UserInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;

/**
 * Created by Semyon.Atamas on 4/24/2015.
 */
public class AuthorizationJetAccountHelper extends AuthorizationHelper {
    public AuthorizationJetAccountHelper(String host) {
        super(host);
    }

    @Override
    public String getAuthorizationUrl() {
        try {
            return "http://account.jetbrains.com/jwt-auth/kotlin-web-demo?auth_url=" +
                    URLEncoder.encode(getCallbackUrl(), "UTF8");
        } catch (UnsupportedEncodingException e) {
            ErrorWriter.getInstance().writeExceptionToExceptionAnalyzer(e, "AUTHORIZATION", "", "");
            return "http://" + host;
        }
    }

    @Nullable
    @Override
    public UserInfo verify(String oauthVerifier) {
        UserInfo userInfo = null;
        try {
            JWSObject jwsObject = JWSObject.parse(oauthVerifier);
            jwsObject.verify(new MACVerifier(ApplicationSettings.JET_ACCOUNT_CREDENTIALS.SECRET));
            JSONObject payload = jwsObject.getPayload().toJSONObject();
            userInfo = new UserInfo();
            userInfo.login(payload.get("name").toString(), payload.get("email").toString(), "jba");
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (JOSEException e) {
            e.printStackTrace();
        }
        return userInfo;
    }

    @NotNull
    @Override
    protected String getType() {
        return "jba";
    }
}
