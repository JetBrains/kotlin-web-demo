package web.view.ukhorskaya.responseHelpers;

import com.intellij.psi.PsiFile;
import org.json.JSONArray;
import web.view.ukhorskaya.Interval;
import web.view.ukhorskaya.errorsDescriptors.ErrorAnalyzer;
import web.view.ukhorskaya.errorsDescriptors.ErrorDescriptor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 9/29/11
 * Time: 4:25 PM
 */

public class JsonResponseForHighlighting {

    private final PsiFile currentPsiFile;

    public JsonResponseForHighlighting(PsiFile currentPsiFile) {
        this.currentPsiFile = currentPsiFile;
    }

    public String getResult() {
        ErrorAnalyzer analyzer = new ErrorAnalyzer(currentPsiFile);
        List<ErrorDescriptor> errorDescriptors = analyzer.getAllErrors();
        JSONArray resultArray = new JSONArray();
        for (ErrorDescriptor errorDescriptor : errorDescriptors) {
            resultArray.put(getMapForJsonResponse(errorDescriptor.getInterval(), errorDescriptor.getMessage(),
                    errorDescriptor.getClassName(), errorDescriptor.getSeverity().name()));
        }
        return resultArray.toString();
    }

    private Map<String, String> getMapForJsonResponse(Interval interval, String titleName, String className, String severity) {
        Map<String, String> map = new HashMap<String, String>();

        map.put("x", "{line: " + interval.startPoint.line + ", ch: " + interval.startPoint.charNumber + "}");
        map.put("y", "{line: " + interval.endPoint.line + ", ch: " + interval.endPoint.charNumber + "}");
        map.put("titleName", escape(titleName));
        map.put("className", className);
        map.put("severity", severity);
        return map;
    }


    private String escape(String str) {
        if ((str != null) && (str.contains("\""))) {
            str = str.replaceAll("\\\"", "'");
        }
        return str;
    }

}
