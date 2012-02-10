package org.jetbrains.demo.ukhorskaya.responseHelpers;

import org.jetbrains.demo.ukhorskaya.ErrorWriter;
import org.jetbrains.demo.ukhorskaya.session.SessionInfo;
import org.jetbrains.k2js.facade.K2JSTranslatorUtils;
import org.json.JSONArray;

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
        K2JSTranslatorUtils translator = new K2JSTranslatorUtils();
        //TODO environment
//        translator.setEnvironment(Initializer.getEnvironment());
        try {
            map.put("text", translator.translateToJS(code, arguments));
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.CONVERT_TO_JS.name(), code + "\n" + arguments);
            map.put("exception", e.getMessage());
        }
        result.put(map);
        return result.toString();
    }
}
