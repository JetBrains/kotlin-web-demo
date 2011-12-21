package web.view.ukhorskaya.responseHelpers;

import org.jetbrains.k2js.K2JSTranslator;
import org.json.JSONArray;
import web.view.ukhorskaya.ErrorWriter;
import web.view.ukhorskaya.ErrorWriterOnServer;
import web.view.ukhorskaya.Initializer;
import web.view.ukhorskaya.session.SessionInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 12/20/11
 * Time: 5:19 PM
 */
public class JsConverter {
    private final SessionInfo info;

    public JsConverter(SessionInfo info) {
        this.info = info;
    }

    public String getResult(String code, String arguments) {
        JSONArray result = new JSONArray();
        Map<String, String> map = new HashMap<String, String>();
        K2JSTranslator translator = new K2JSTranslator();
        translator.setEnvironment(Initializer.getEnvironment());
        try {
            map.put("text", translator.translateStringWithCallToMain(code, arguments));
        } catch (Throwable e) {
            ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(info.getType(),
                    e, code + "\n" + arguments));
            map.put("exception", e.getMessage());
        }
        result.put(map);
        return result.toString();
    }
}
