package org.jetbrains.demo.ukhorskaya.authorization;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.demo.ukhorskaya.ErrorWriter;
import org.jetbrains.demo.ukhorskaya.ErrorWriterOnServer;
import org.jetbrains.demo.ukhorskaya.ResponseUtils;
import org.jetbrains.demo.ukhorskaya.server.ServerSettings;
import org.jetbrains.demo.ukhorskaya.session.SessionInfo;
import org.jetbrains.demo.ukhorskaya.session.UserInfo;
import org.jetbrains.demo.ukhorskaya.handlers.ServerHandler;
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
//            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("Cannot authorize authorization request", e, "null"));
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
//            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("Cannot verify authorization request", e, url));
        }
        return userInfo;
    }

}
