package org.jetbrains.demo.ukhorskaya.responseHelpers;

import com.intellij.psi.PsiFile;
import org.jetbrains.demo.ukhorskaya.ErrorWriter;
import org.jetbrains.demo.ukhorskaya.Interval;
import org.jetbrains.demo.ukhorskaya.ResponseUtils;
import org.jetbrains.demo.ukhorskaya.errorsDescriptors.ErrorAnalyzer;
import org.jetbrains.demo.ukhorskaya.errorsDescriptors.ErrorDescriptor;
import org.jetbrains.demo.ukhorskaya.exceptions.KotlinCoreException;
import org.jetbrains.demo.ukhorskaya.server.ServerSettings;
import org.jetbrains.demo.ukhorskaya.session.SessionInfo;
import org.json.JSONArray;

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
    
    private final SessionInfo sessionInfo;

    public JsonResponseForHighlighting(PsiFile currentPsiFile, SessionInfo info) {
        this.currentPsiFile = currentPsiFile;
        this.sessionInfo = info;
    }

    public String getResultFormK2Js() {
        ErrorAnalyzer analyzer = new ErrorAnalyzer(currentPsiFile, sessionInfo);
        List<ErrorDescriptor> errorDescriptors;
        try {
            errorDescriptors = analyzer.getAllErrorsFromK2Js();
        } catch (KotlinCoreException e) {
            return ResponseUtils.getErrorWithStackTraceInJson(ServerSettings.KOTLIN_ERROR_MESSAGE
                    , e.getStackTraceString());
        }
        JSONArray resultArray = new JSONArray();

        for (ErrorDescriptor errorDescriptor : errorDescriptors) {
            resultArray.put(getMapForJsonResponse(errorDescriptor.getInterval(), errorDescriptor.getMessage(),
                    errorDescriptor.getClassName(), errorDescriptor.getSeverity().name()));
        }
        return ResponseUtils.escapeString(resultArray.toString());
    }

    public String getResult() {
        ErrorAnalyzer analyzer = new ErrorAnalyzer(currentPsiFile, sessionInfo);
        List<ErrorDescriptor> errorDescriptors;
        try {
            errorDescriptors = analyzer.getAllErrors();
        } catch (KotlinCoreException e) {
            return ResponseUtils.getErrorWithStackTraceInJson(ServerSettings.KOTLIN_ERROR_MESSAGE
                     , e.getStackTraceString());
        }
        JSONArray resultArray = new JSONArray();

        for (ErrorDescriptor errorDescriptor : errorDescriptors) {
            resultArray.put(getMapForJsonResponse(errorDescriptor.getInterval(), errorDescriptor.getMessage(),
                    errorDescriptor.getClassName(), errorDescriptor.getSeverity().name()));
        }
        return ResponseUtils.escapeString(resultArray.toString());
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
