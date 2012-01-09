package web.view.ukhorskaya.authorization;

import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import web.view.ukhorskaya.ResponseUtils;
import web.view.ukhorskaya.server.KotlinHttpServer;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 12/30/11
 * Time: 3:54 PM
 */

public class AuthorizationTwitterHelper extends AuthorizationHelper {

    private static OAuthService twitterService;
    private static Token requestToken;

    public String authorize() {
        try {
            twitterService = new ServiceBuilder()
                    .provider(TwitterApi.class)
                    .apiKey("g0dAeSZpnxTHxRKV2UZFGg")
                    .apiSecret("NSfUf8o3BhyT96U6hcCarWIUEwz6Le4FY6Em7WBPtuw")
                    .callback("http://" + KotlinHttpServer.getHost() + "/login.html?twitter")
                    .build();
            requestToken = twitterService.getRequestToken();
            String authUrl = twitterService.getAuthorizationUrl(requestToken);
            System.out.println(authUrl);
            return authUrl;
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return "";
    }

    public void verify(String url) {
        try {
            String authUrl = ResponseUtils.substringAfter(url, "oauth_verifier=");
            String authToken = ResponseUtils.substringBetween(url, "oauth_token=", "oauth_verifier=");
            Verifier verifier = new Verifier(authUrl);
            Token accessToken = twitterService.getAccessToken(requestToken, verifier); // the requestToken you had from step 2
            OAuthRequest request = new OAuthRequest(Verb.GET, "http://api.twitter.com/1/account/verify_credentials.xml");
            twitterService.signRequest(accessToken, request); // the access token from step 4
            Response response = request.send();
            System.out.println(response.getBody());

            Document document = ResponseUtils.getXmlDocument(response.getStream());
            if (document == null) {
                return;
            }
            NodeList nodeList = document.getElementsByTagName("user");
            for (int i = 0; i < nodeList.getLength(); i++) {
                if (nodeList.item(i).getNodeName().equals("name")) {
                    userName = nodeList.item(i).getTextContent();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getUserName() {
        return userName;
    }
}
