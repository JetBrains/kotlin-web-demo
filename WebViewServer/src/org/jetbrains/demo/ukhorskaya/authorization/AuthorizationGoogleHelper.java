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
import org.scribe.builder.api.GoogleApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 12/30/11
 * Time: 3:54 PM
 */

public class AuthorizationGoogleHelper extends AuthorizationHelper {
    private final String TYPE = "google";

    private static OAuthService googleService;

    private static Token requestToken;

    private static final String AUTHORIZE_URL = "https://www.google.com/accounts/OAuthAuthorizeToken?oauth_token=";
    private static final String PROTECTED_RESOURCE_URL = "https://www.googleapis.com/oauth2/v2/userinfo";
    private static final String SCOPE = "https://www.googleapis.com/auth/userinfo.profile";

    public String authorize() {
        try {
            googleService = new ServiceBuilder()
                    .provider(GoogleApi.class)
                    .apiKey("anonymous")
                    .apiSecret("anonymous")
                    .scope(SCOPE)
                    .callback("http://" + ServerSettings.AUTH_REDIRECT + ResponseUtils.generateRequestString("authorization", "google"))
                    .build();

            requestToken = googleService.getRequestToken();
            return googleService.getAuthorizationUrl(requestToken);
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.AUTHORIZATION.name(), "google");
//            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("Cannot authorize authorization request", e, "null"));
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
            JSONObject object = new JSONObject(response.getBody());
            userInfo.login((String) object.get("name"), (String) object.get("id"), TYPE);

        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, SessionInfo.TypeOfRequest.AUTHORIZATION.name(), "google: " + url);
//            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog("Cannot verify authorization request", e, url));
        }
        return userInfo;
    }

}
