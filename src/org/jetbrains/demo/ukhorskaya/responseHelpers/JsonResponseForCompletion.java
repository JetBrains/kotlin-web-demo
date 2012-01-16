package org.jetbrains.demo.ukhorskaya.responseHelpers;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.demo.ukhorskaya.ErrorWriter;
import org.jetbrains.demo.ukhorskaya.MyDeclarationDescriptorVisitor;
import org.jetbrains.demo.ukhorskaya.ResponseUtils;
import org.jetbrains.demo.ukhorskaya.exceptions.KotlinCoreException;
import org.jetbrains.demo.ukhorskaya.server.ServerSettings;
import org.jetbrains.demo.ukhorskaya.session.SessionInfo;
import org.jetbrains.jet.compiler.TipsManager;
import org.jetbrains.jet.lang.cfg.pseudocode.JetControlFlowDataTraceFactory;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.java.AnalyzerFacade;
import org.jetbrains.jet.lang.resolve.scopes.JetScope;
import org.jetbrains.jet.lang.types.JetType;
import org.jetbrains.jet.resolve.DescriptorRenderer;
import org.json.JSONArray;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/20/11
 * Time: 1:53 PM
 */

public class JsonResponseForCompletion {
    private final int NUMBER_OF_CHAR_IN_COMPLETION_NAME = 40;

    private final Project currentProject;
    private PsiFile currentPsiFile;
    private Document currentDocument;
    private final int lineNumber;
    private final int charNumber;
    private int caretPositionOffset;
    private SessionInfo sessionInfo;


    public JsonResponseForCompletion(int lineNumber, int charNumber, PsiFile currentPsiFile, SessionInfo sessionInfo) {
        this.lineNumber = lineNumber;
        this.charNumber = charNumber;
        this.currentPsiFile = currentPsiFile;
        this.currentProject = currentPsiFile.getProject();
        this.currentDocument = currentPsiFile.getViewProvider().getDocument();
        this.sessionInfo = sessionInfo;
    }

    public String getResult() {
        addExpressionAtCaret();

        sessionInfo.getTimeManager().saveCurrentTime();
        BindingContext bindingContext;
        try {
            /*bindingContext = AnalyzingUtils.analyzeNamespaces(currentProject, Configuration.EMPTY,
                    Collections.singletonList(((JetFile) currentPsiFile).getRootNamespace()),
                    Predicates.<PsiFile>equalTo(currentPsiFile), JetControlFlowDataTraceFactory.EMPTY,
                    JetSemanticServices.createSemanticServices(currentProject));*/
            /*bindingContext = AnalyzerFacade.analyzeNamespacesWithJavaIntegration(
                                currentProject,
                                Collections.singletonList(((JetFile) currentPsiFile).getRootNamespace()),
                                Predicates.<PsiFile>equalTo(currentPsiFile),
                                JetControlFlowDataTraceFactory.EMPTY);*/
            bindingContext = AnalyzerFacade.analyzeOneFileWithJavaIntegration(
                    (JetFile) currentPsiFile, JetControlFlowDataTraceFactory.EMPTY);
        } catch (Throwable e) {
            String exception = ErrorWriter.getExceptionForLog(sessionInfo.getType(), e, currentPsiFile.getText());
            ErrorWriter.ERROR_WRITER.writeException(exception);
            return ResponseUtils.getErrorInJson(ServerSettings.KOTLIN_ERROR_MESSAGE
                    + ResponseUtils.addNewLine() + new KotlinCoreException(e).getStackTraceString());
        }
        String info = ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(), sessionInfo.getId(), "ANALYZE namespaces " + sessionInfo.getTimeManager().getMillisecondsFromSavedTime() + " size: " + currentPsiFile.getTextLength());
        ErrorWriter.ERROR_WRITER.writeInfo(info);
        PsiElement element = getExpressionForScope();
        if (element == null) {
            return "[]";
        }

        Collection<DeclarationDescriptor> descriptors = null;
        try {
            if (element.getParent() instanceof JetSimpleNameExpression) {
                descriptors = TipsManager.getReferenceVariants((JetSimpleNameExpression) element.getParent(), bindingContext);
            } else {
                JetScope resolutionScope;
                PsiElement parent = element.getParent();
                if (parent instanceof JetQualifiedExpression) {
                    JetQualifiedExpression qualifiedExpression = (JetQualifiedExpression) parent;
                    JetExpression receiverExpression = qualifiedExpression.getReceiverExpression();

                    final JetType expressionType = bindingContext.get(BindingContext.EXPRESSION_TYPE, receiverExpression);
                    resolutionScope = bindingContext.get(BindingContext.RESOLUTION_SCOPE, receiverExpression);

                    if (expressionType != null && resolutionScope != null) {
                        //                    descriptors = resolutionScope.getAllDescriptors();
                        //                    descriptors.addAll(expressionType.getMemberScope().getAllDescriptors());
                        descriptors = expressionType.getMemberScope().getAllDescriptors();
                    }
                } else {
                    resolutionScope = bindingContext.get(BindingContext.RESOLUTION_SCOPE, (JetExpression) element);
                    if (resolutionScope != null) {
                        descriptors = resolutionScope.getAllDescriptors();
                    } else {
                        return "[]";
                    }
                }
            }

        } catch (Throwable e) {
            String exception = ErrorWriter.getExceptionForLog(sessionInfo.getType(), e, currentPsiFile.getText());
            ErrorWriter.ERROR_WRITER.writeException(exception);
//            e.printStackTrace();
            return "[]";
        }

        JSONArray jsonArray = new JSONArray();
        if (descriptors != null) {
            for (DeclarationDescriptor descriptor : descriptors) {
                Map<String, String> map = new HashMap<String, String>();
                map.put("icon", getIconFromDescriptor(descriptor));
                map.put("tail", "   " + getTailText(descriptor));
                map.put("name", getNameFromDescriptor(descriptor));

                jsonArray.put(map);
            }
        } else {
            String exception = ErrorWriter.getExceptionForLog(sessionInfo.getType(), "Resolution scope is null.", currentPsiFile.getText());
            ErrorWriter.ERROR_WRITER.writeException(exception);
            return "[]";
        }

        /*JetScope resolutionScope;
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
                map.put("tail", "   " + getTailText(descriptor));
                map.put("name", getNameFromDescriptor(descriptor));

                jsonArray.put(map);
            }
        } else {
            String exception = ErrorWriter.getExceptionForLog(sessionInfo.getType(), "Resolution scope is null.", currentPsiFile.getText());
            ErrorWriter.ERROR_WRITER.writeException(exception);
        } */
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
                String exception = ErrorWriter.getExceptionForLog(sessionInfo.getType(), " Cannot find an element for take completion.", currentPsiFile.getText());
                ErrorWriter.ERROR_WRITER.writeException(exception);
                break;
            }
        }
        return element;
    }

    private String getNameFromDescriptor(DeclarationDescriptor descriptor) {
        MyDeclarationDescriptorVisitor descriptorVisitor = new MyDeclarationDescriptorVisitor();
        StringBuilder builder = new StringBuilder();
        descriptor.accept(descriptorVisitor, builder);
        return formatName(builder, NUMBER_OF_CHAR_IN_COMPLETION_NAME).toString();
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
