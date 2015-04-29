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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.session.UserInfo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public abstract class AuthorizationHelper {
    protected static final int TIMEOUT = 500;
    protected String type;
    protected String host;

    public AuthorizationHelper(String host) {
        this.host = host;
    }

    public static AuthorizationHelper getHelper(String type, String host) {
        switch (type) {
            case "twitter":
                return new AuthorizationTwitterHelper(host);
            case "google":
                return new AuthorizationGoogleHelper(host);
            case "facebook":
                return new AuthorizationFacebookHelper(host);
            case "github":
                return new AuthorizationGithubHelper(host);
            case "jba":
                return new AuthorizationJetAccountHelper(host);
            default:
                throw new IllegalArgumentException("Unknown authorization type");
        }
    }

    public abstract String getAuthorizationUrl();

    @Nullable
    public abstract UserInfo verify(String oauthVerifier);

    protected String getCallbackUrl() {
        return "http://" + host + "/verify/" + getType();
    }

    @NotNull
    protected abstract String getType();
}
