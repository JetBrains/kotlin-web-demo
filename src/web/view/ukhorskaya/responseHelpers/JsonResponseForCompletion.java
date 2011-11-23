package web.view.ukhorskaya.responseHelpers;

import com.google.common.base.Predicates;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.apache.log4j.Logger;
import org.jetbrains.jet.lang.cfg.pseudocode.JetControlFlowDataTraceFactory;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.psi.JetExpression;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.psi.JetPsiFactory;
import org.jetbrains.jet.lang.psi.JetQualifiedExpression;
import org.jetbrains.jet.lang.resolve.AnalyzingUtils;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.java.JavaDefaultImports;
import org.jetbrains.jet.lang.resolve.scopes.JetScope;
import org.jetbrains.jet.lang.types.JetType;
import org.jetbrains.jet.resolve.DescriptorRenderer;
import org.json.JSONArray;
import web.view.ukhorskaya.ErrorsWriter;
import web.view.ukhorskaya.MyDeclarationDescriptorVisitor;
import web.view.ukhorskaya.ResponseUtils;
import web.view.ukhorskaya.sessions.HttpSession;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/20/11
 * Time: 1:53 PM
 */

public class JsonResponseForCompletion {
    private final Logger LOG = Logger.getLogger(JsonResponseForCompletion.class);

    private final Project currentProject;
    private PsiFile currentPsiFile;
    private Document currentDocument;
    private final int lineNumber;
    private final int charNumber;
    private int caretPositionOffset;

    public JsonResponseForCompletion(int lineNumber, int charNumber, PsiFile currentPsiFile) {
        this.lineNumber = lineNumber;
        this.charNumber = charNumber;
        this.currentPsiFile = currentPsiFile;
        this.currentProject = currentPsiFile.getProject();
        this.currentDocument = currentPsiFile.getViewProvider().getDocument();
    }

    public String getResult() {
        addExpressionAtCaret();

        HttpSession.TIME_MANAGER.saveCurrentTime();
        BindingContext bindingContext;
        try {
            bindingContext = AnalyzingUtils.getInstance(JavaDefaultImports.JAVA_DEFAULT_IMPORTS).analyzeNamespaces(
                    currentProject,
                    Collections.singletonList(((JetFile) currentPsiFile).getRootNamespace()),
                    Predicates.<PsiFile>equalTo(currentPsiFile),
                    JetControlFlowDataTraceFactory.EMPTY);
        } catch (Throwable e) {
            ErrorsWriter.LOG_FOR_EXCEPTIONS.error(ErrorsWriter.getExceptionForLog("analyze", e.getMessage(), currentPsiFile.getText()), e);
            return ResponseUtils.getErrorInJson("Error: bug was reported to developers.");
        }
        LOG.info("userId=" + HttpSession.SESSION_ID + " ANALYZE namespaces " + HttpSession.TIME_MANAGER.getMillisecondsFromSavedTime());

        PsiElement element = getExpressionForScope();
        PsiElement parent = element.getParent();

        JetScope resolutionScope;
        if (parent instanceof JetQualifiedExpression) {
            JetQualifiedExpression qualifiedExpression = (JetQualifiedExpression) parent;
            JetExpression receiverExpression = qualifiedExpression.getReceiverExpression();
            final JetType expressionType = bindingContext.get(BindingContext.EXPRESSION_TYPE, receiverExpression);
            if (expressionType != null) {
                resolutionScope = expressionType.getMemberScope();
            } else {
                resolutionScope = null;
            }
        } else {
            resolutionScope = bindingContext.get(BindingContext.RESOLUTION_SCOPE, (JetExpression) element);
        }

        JSONArray jsonArray = new JSONArray();
        if (resolutionScope != null) {
            Collection<DeclarationDescriptor> descriptors = resolutionScope.getAllDescriptors();
            for (DeclarationDescriptor descriptor : descriptors) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("icon", getIconFromDescriptor(descriptor));
                map.put("tail", getTailText(descriptor));
                map.put("name", getNameFromDescriptor(descriptor));

                jsonArray.put(map);
            }
        } else {
            LOG.error("userId=" + HttpSession.SESSION_ID + " resolutionScope is null: " + currentPsiFile.getText());
        }
        return jsonArray.toString();
    }

    private String getIconFromDescriptor(DeclarationDescriptor descriptor) {
        if (descriptor instanceof FunctionDescriptor) {
            return "/icons/method.png";
        } else if ((descriptor instanceof PropertyDescriptor) || (descriptor instanceof LocalVariableDescriptor)) {
            return "/icons/property.png";
        } else if (descriptor instanceof ClassDescriptor) {
            return "/icons/class.png";
        } else if (descriptor instanceof ValueParameterDescriptor) {
            return "/icons/genericValue.png";
        }
        return "";
    }

    private PsiElement getExpressionForScope() {
        PsiElement element = currentPsiFile.findElementAt(caretPositionOffset);
        while (!(element instanceof JetExpression)) {
            if (element != null) {
                element = element.getParent();
            } else {
                LOG.error("userId=" + HttpSession.SESSION_ID + " Cannot find an element for take completion.");
                break;
            }
        }
        return element;
    }

    private String getNameFromDescriptor(DeclarationDescriptor descriptor) {
        MyDeclarationDescriptorVisitor descriptorVisitor = new MyDeclarationDescriptorVisitor();
        StringBuilder builder = new StringBuilder();
        descriptor.accept(descriptorVisitor, builder);
        return formatName(builder, 40).toString();
    }

    private StringBuilder formatName(StringBuilder builder, int symbols) {
        if (builder.length() > symbols) {
            StringBuilder resultBuilder = new StringBuilder();
            resultBuilder.append(builder.substring(0, symbols));
            resultBuilder.append("...");
            return resultBuilder;
        }
        return builder;
    }

    private void addExpressionAtCaret() {
        caretPositionOffset = getOffsetFromLineAndChar(lineNumber, charNumber);
        String text = currentPsiFile.getText();
        if (caretPositionOffset != 0) {
            StringBuilder buffer = new StringBuilder(text.substring(0, caretPositionOffset));
            buffer.append("IntellijIdeaRulezzz ");
            buffer.append(text.substring(caretPositionOffset));
            currentPsiFile = JetPsiFactory.createFile(currentProject, buffer.toString());
            currentDocument = currentPsiFile.getViewProvider().getDocument();
        }
    }

    private int getOffsetFromLineAndChar(int line, int charNumber) {
        int lineStart = currentDocument.getLineStartOffset(line);
        return lineStart + charNumber;
    }

    private String getTailText(DeclarationDescriptor descriptor) {
        String tailText = "";
        if (descriptor instanceof FunctionDescriptor) {
            FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;
            JetType returnType = functionDescriptor.getReturnType();
            tailText = DescriptorRenderer.TEXT.renderType(returnType);
        } else if (descriptor instanceof VariableDescriptor) {
            JetType outType = ((VariableDescriptor) descriptor).getOutType();
            tailText = DescriptorRenderer.TEXT.renderType(outType);
        }
        return tailText;
    }
}
