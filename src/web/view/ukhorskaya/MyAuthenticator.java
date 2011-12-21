package web.view.ukhorskaya;

import com.sun.net.httpserver.Authenticator;
import com.sun.net.httpserver.BasicAuthenticator;
import com.sun.net.httpserver.HttpExchange;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 12/21/11
 * Time: 12:51 PM
 */
public class MyAuthenticator extends BasicAuthenticator {

    @Override
    public Result authenticate(HttpExchange exchange) {

        return super.authenticate(exchange);    //To change body of overridden methods use File | Settings | File Templates.
    }

    public MyAuthenticator(String s) {
        super(s);
    }

    @Override
    public boolean checkCredentials(String s, String s1) {
        if (s.equals("root")) {
            return true;
        }
        return false;
    }
}
