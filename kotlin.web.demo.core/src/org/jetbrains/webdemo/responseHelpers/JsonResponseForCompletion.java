/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.webdemo.responseHelpers;

import org.jetbrains.jet.internal.com.intellij.openapi.editor.Document;
import org.jetbrains.jet.internal.com.intellij.openapi.project.Project;
import org.jetbrains.jet.internal.com.intellij.psi.PsiElement;
import org.jetbrains.jet.internal.com.intellij.psi.PsiFile;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.psi.*;
import org.jetbrains.jet.lang.resolve.AnalyzerScriptParameter;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.java.AnalyzerFacadeForJVM;
import org.jetbrains.jet.lang.resolve.scopes.JetScope;
import org.jetbrains.jet.lang.types.JetType;
import org.jetbrains.jet.plugin.codeInsight.TipsManager;
import org.jetbrains.jet.renderer.DescriptorRenderer;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.MyDeclarationDescriptorVisitor;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.translator.WebDemoTranslatorFacade;
import org.json.JSONArray;

import java.util.*;

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
        try {
            addExpressionAtCaret();
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.COMPLETE.name(), currentPsiFile.getText() + "  " + lineNumber + " " + charNumber);
            return "[]";
        }
        /* int i = 0;

        if (i == 0) {
            throw new NullPointerException("Test Attachments");
        }*/
        sessionInfo.getTimeManager().saveCurrentTime();
        BindingContext bindingContext;
        try {
            if (sessionInfo.getRunConfiguration().equals(SessionInfo.RunConfiguration.CANVAS)) {
                bindingContext = WebDemoTranslatorFacade.analyzeProgramCode((JetFile) currentPsiFile);
            } else {
                bindingContext = AnalyzerFacadeForJVM.analyzeOneFileWithJavaIntegration(
                        (JetFile) currentPsiFile, Collections.<AnalyzerScriptParameter>emptyList()).getBindingContext();
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), currentPsiFile.getText());
            return ResponseUtils.getErrorInJson(ApplicationSettings.KOTLIN_ERROR_MESSAGE
                    + ResponseUtils.addNewLine() + new KotlinCoreException(e).getStackTraceString());
        }
        String info = ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(), sessionInfo.getId(), "ANALYZE namespaces " + sessionInfo.getTimeManager().getMillisecondsFromSavedTime() + " size: " + currentPsiFile.getTextLength());
        ErrorWriter.ERROR_WRITER.writeInfo(info);

        if (bindingContext == null) {
            return "[]";
        }
        PsiElement element = getExpressionForScope();
        if (element == null) {
            return "[]";
        }

        Collection<DeclarationDescriptor> descriptors = null;
        boolean isTipsManagerCompletion = true;
        try {
            if (element instanceof JetSimpleNameExpression) {
                descriptors = TipsManager.getReferenceVariants((JetSimpleNameExpression) element, bindingContext);
            } else if (element.getParent() instanceof JetSimpleNameExpression) {
                descriptors = TipsManager.getReferenceVariants((JetSimpleNameExpression) element.getParent(), bindingContext);
            } else {
                isTipsManagerCompletion = false;
                JetScope resolutionScope;
                PsiElement parent = element.getParent();
                if (parent instanceof JetQualifiedExpression) {
                    JetQualifiedExpression qualifiedExpression = (JetQualifiedExpression) parent;
                    JetExpression receiverExpression = qualifiedExpression.getReceiverExpression();

                    final JetType expressionType = bindingContext.get(BindingContext.EXPRESSION_TYPE, receiverExpression);
                    resolutionScope = bindingContext.get(BindingContext.RESOLUTION_SCOPE, receiverExpression);

                    if (expressionType != null && resolutionScope != null) {
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
            e.printStackTrace();
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    sessionInfo.getType(), currentPsiFile.getText());
            return "[]";
        }

        JSONArray jsonArray = new JSONArray();
        if (descriptors != null) {
            String prefix;
            if (isTipsManagerCompletion) {
                prefix = element.getText();
            } else {
                prefix = element.getParent().getText();
            }
            prefix = ResponseUtils.substringBefore(prefix, "IntellijIdeaRulezzz");
            if (prefix.endsWith(".")) {
                prefix = "";
            }

            if (!(descriptors instanceof ArrayList)) {
                descriptors = new ArrayList<DeclarationDescriptor>(descriptors);
            }

            Collections.sort((ArrayList<DeclarationDescriptor>) descriptors, new Comparator<DeclarationDescriptor>() {
                @Override
                public int compare(DeclarationDescriptor d1, DeclarationDescriptor d2) {
                    return getNameFromDescriptor(d1).compareToIgnoreCase(getNameFromDescriptor(d2));
                }
            });

            for (DeclarationDescriptor descriptor : descriptors) {
                String name = getNameFromDescriptor(descriptor);
                if (prefix.isEmpty() || name.startsWith(prefix)) {
                    Map<String, String> map = new HashMap<String, String>();
                    map.put("icon", getIconFromDescriptor(descriptor));
                    map.put("tail", getTailText(descriptor));
                    map.put("name", name);

                    jsonArray.put(map);
                }
            }
        }

        return jsonArray.toString();
    }

    private String getIconFromDescriptor(DeclarationDescriptor descriptor) {
        if (descriptor instanceof FunctionDescriptor) {
            return "method";
        } else if ((descriptor instanceof PropertyDescriptor) || (descriptor instanceof LocalVariableDescriptor)) {
            return "property";
        } else if (descriptor instanceof ClassDescriptor) {
            return "class";
        } else if (descriptor instanceof AbstractNamespaceDescriptorImpl) {
            return "package";
        } else if (descriptor instanceof ValueParameterDescriptor) {
            return "genericValue";
        } else if (descriptor instanceof TypeParameterDescriptorImpl) {
            return "class";
        } else {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(
                    "Impossible to find icon",
                    descriptor.getName().getName() + " " + descriptor.getClass().toString(), sessionInfo.getType(), "");
            return "";
        }
    }

    private PsiElement getExpressionForScope() {
        PsiElement element = currentPsiFile.findElementAt(caretPositionOffset);
        while (!(element instanceof JetExpression)) {
            if (element != null) {
                element = element.getParent();
            } else {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(new UnsupportedOperationException("Cannot find an element to take a completion"),
                        SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), currentPsiFile.getText()
                );
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
            if (returnType != null) {
                tailText = DescriptorRenderer.TEXT.renderType(returnType);
            }
        } else if (descriptor instanceof VariableDescriptor) {
            JetType outType = ((VariableDescriptor) descriptor).getType();
            tailText = DescriptorRenderer.TEXT.renderType(outType);
        }
        return tailText;
    }
}
