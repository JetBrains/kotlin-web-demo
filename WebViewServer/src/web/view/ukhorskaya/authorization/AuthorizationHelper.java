package web.view.ukhorskaya.authorization;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 12/30/11
 * Time: 1:49 PM
 */
public abstract class AuthorizationHelper {
    protected String userName;

    public abstract String authorize();

    public abstract void verify(String key) ;
    
    public abstract String getUserName();
}
