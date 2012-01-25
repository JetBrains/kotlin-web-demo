package web.view.ukhorskaya.authorization;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.demo.ukhorskaya.session.UserInfo;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 12/30/11
 * Time: 1:49 PM
 */
public abstract class AuthorizationHelper {
    protected String type;

    public abstract String authorize();

    @Nullable
    public abstract UserInfo verify(String url);
}
