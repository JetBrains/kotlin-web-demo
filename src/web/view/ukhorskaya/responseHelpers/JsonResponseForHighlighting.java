package web.view.ukhorskaya.responseHelpers;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import org.jetbrains.jet.lang.cfg.pseudocode.JetControlFlowDataTraceFactory;
import org.jetbrains.jet.lang.diagnostics.Diagnostic;
import org.jetbrains.jet.lang.diagnostics.DiagnosticFactory;
import org.jetbrains.jet.lang.diagnostics.Severity;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.resolve.AnalyzingUtils;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.java.JavaDefaultImports;
import org.json.JSONArray;
import web.view.ukhorskaya.Interval;
import web.view.ukhorskaya.errorsDescriptors.ErrorAnalyzer;
import web.view.ukhorskaya.errorsDescriptors.ErrorDescriptor;

import java.util.*;

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
