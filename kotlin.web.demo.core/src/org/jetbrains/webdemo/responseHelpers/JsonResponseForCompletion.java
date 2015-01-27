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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import kotlin.Function1;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.descriptors.*;
import org.jetbrains.kotlin.descriptors.impl.LocalVariableDescriptor;
import org.jetbrains.kotlin.descriptors.impl.TypeParameterDescriptorImpl;
import org.jetbrains.kotlin.idea.codeInsight.ReferenceVariantsHelper;
import org.jetbrains.kotlin.idea.util.IdeDescriptorRenderers;
import org.jetbrains.kotlin.name.Name;
import org.jetbrains.kotlin.psi.JetExpression;
import org.jetbrains.kotlin.psi.JetFile;
import org.jetbrains.kotlin.psi.JetQualifiedExpression;
import org.jetbrains.kotlin.psi.JetSimpleNameExpression;
import org.jetbrains.kotlin.renderer.DescriptorRenderer;
import org.jetbrains.kotlin.renderer.DescriptorRendererBuilder;
import org.jetbrains.kotlin.renderer.NameShortness;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.DescriptorUtils;
import org.jetbrains.kotlin.resolve.scopes.DescriptorKindFilter;
import org.jetbrains.kotlin.resolve.scopes.JetScope;
import org.jetbrains.kotlin.types.JetType;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.JetPsiFactoryUtil;
import org.jetbrains.webdemo.ResolveUtils;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.translator.WebDemoTranslatorFacade;

import java.util.*;

public class JsonResponseForCompletion {
    private static final DescriptorRenderer RENDERER = new DescriptorRendererBuilder()
            .setNameShortness(NameShortness.SHORT)
            .setTypeNormalizer(IdeDescriptorRenderers.APPROXIMATE_FLEXIBLE_TYPES)
            .setWithoutFunctionParameterNames(true)
            .setRenderDefaultValues(false)
            .build();

    private static final Function1<DeclarationDescriptor, Boolean> VISIBILITY_FILTER = new Function1<DeclarationDescriptor, Boolean>() {
        @Override
        public Boolean invoke(DeclarationDescriptor declarationDescriptor) {
            return true;
        }
    };
    private static final Function1<Name, Boolean> NAME_FILTER = new Function1<Name, Boolean>() {
        @Override
        public Boolean invoke(Name name) {
            return true;
        }
    };
    private final int NUMBER_OF_CHAR_IN_COMPLETION_NAME = 40;
    private final Project currentProject;
    private final int lineNumber;
    private final int charNumber;
    private PsiFile currentPsiFile;
    private Document currentDocument;
    private int caretPositionOffset;
    private SessionInfo sessionInfo;
    private List<PsiFile> psiFiles;

    public JsonResponseForCompletion(List<PsiFile> psiFiles, SessionInfo sessionInfo, String filename, int lineNumber, int charNumber) {
        this.lineNumber = lineNumber;
        this.charNumber = charNumber;
        this.psiFiles = psiFiles;
        for (PsiFile file : psiFiles) {
            if (file.getName().equals(filename)) {
                currentPsiFile = file;
            }
        }
        this.currentProject = currentPsiFile.getProject();
        this.currentDocument = currentPsiFile.getViewProvider().getDocument();
        this.sessionInfo = sessionInfo;
    }

    // see DescriptorLookupConverter.createLookupElement
    @NotNull
    public static Pair<String, String> getPresentableText(@NotNull DeclarationDescriptor descriptor) {
        String presentableText = descriptor.getName().asString();
        String typeText = "";
        String tailText = "";

        if (descriptor instanceof FunctionDescriptor) {
            FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;
            JetType returnType = functionDescriptor.getReturnType();
            typeText = returnType != null ? RENDERER.renderType(returnType) : "";
            presentableText += RENDERER.renderFunctionParameters(functionDescriptor);

            boolean extensionFunction = functionDescriptor.getExtensionReceiverParameter() != null;
            DeclarationDescriptor containingDeclaration = descriptor.getContainingDeclaration();
            if (containingDeclaration != null && extensionFunction) {
                tailText += " for " + RENDERER.renderType(functionDescriptor.getExtensionReceiverParameter().getType());
                tailText += " in " + DescriptorUtils.getFqName(containingDeclaration);
            }
        } else if (descriptor instanceof VariableDescriptor) {
            JetType outType = ((VariableDescriptor) descriptor).getType();
            typeText = RENDERER.renderType(outType);
        } else if (descriptor instanceof ClassDescriptor) {
            DeclarationDescriptor declaredIn = descriptor.getContainingDeclaration();
            assert declaredIn != null;
            tailText = " (" + DescriptorUtils.getFqName(declaredIn) + ")";
        } else {
            typeText = RENDERER.render(descriptor);
        }

        if (typeText.isEmpty()) {
            return new Pair<String, String>(presentableText, tailText);
        } else {
            return new Pair<String, String>(presentableText, typeText);
        }
    }

    public String getResult() {
        try {
            addExpressionAtCaret();
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.COMPLETE.name(), sessionInfo.getOriginUrl(), currentPsiFile.getText() + "  " + lineNumber + " " + charNumber);
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
                bindingContext = WebDemoTranslatorFacade.analyzeProgramCode(convertList(psiFiles), sessionInfo);
            } else {
                bindingContext = ResolveUtils.getBindingContext(convertList(psiFiles), currentProject);
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
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
            ReferenceVariantsHelper helper = new ReferenceVariantsHelper(bindingContext, VISIBILITY_FILTER);
            if (element instanceof JetSimpleNameExpression) {
                descriptors = helper.getReferenceVariants((JetSimpleNameExpression) element, DescriptorKindFilter.ALL, false, NAME_FILTER);
            } else if (element.getParent() instanceof JetSimpleNameExpression) {
                descriptors = helper.getReferenceVariants((JetSimpleNameExpression) element.getParent(), DescriptorKindFilter.ALL, false, NAME_FILTER);
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
                    sessionInfo.getType(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
            return "[]";
        }

        ArrayNode jsonArray = new ArrayNode(JsonNodeFactory.instance);
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
                    Pair<String, String> d1PresText = getPresentableText(d1);
                    Pair<String, String> d2PresText = getPresentableText(d2);
                    return (d1PresText.getFirst() + d1PresText.getSecond()).compareToIgnoreCase((d2PresText.getFirst() + d2PresText.getSecond()));
                }
            });

            for (DeclarationDescriptor descriptor : descriptors) {
                Pair<String, String> presentableText = getPresentableText(descriptor);
                String name = formatName(presentableText.getFirst(), NUMBER_OF_CHAR_IN_COMPLETION_NAME);
                if (prefix.isEmpty() || name.startsWith(prefix)) {
                    ObjectNode jsonObject = jsonArray.addObject();
                    jsonObject.put("icon", getIconFromDescriptor(descriptor));
                    jsonObject.put("name", name);
                    jsonObject.put("tail", presentableText.getSecond());

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
        } else if (descriptor instanceof PackageFragmentDescriptor || descriptor instanceof PackageViewDescriptor) {
            return "package";
        } else if (descriptor instanceof ValueParameterDescriptor) {
            return "genericValue";
        } else if (descriptor instanceof TypeParameterDescriptorImpl) {
            return "class";
        } else {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer("Impossible to find icon",
                    descriptor.getName().asString() + " " + descriptor.getClass().toString(), sessionInfo.getType(), sessionInfo.getOriginUrl(), "");
            return "";
        }
    }

    private PsiElement getExpressionForScope() {
        PsiElement element = currentPsiFile.findElementAt(caretPositionOffset);
        while (!(element instanceof JetExpression)) {
            if (element != null) {
                element = element.getParent();
            } else {
                if (currentPsiFile.getText().length() > 5) {
                    ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(new UnsupportedOperationException("Cannot find an element to take a completion"),
                            SessionInfo.TypeOfRequest.ANALYZE_LOG.name(), sessionInfo.getOriginUrl(),
                            currentPsiFile.getText());
                }
                break;
            }
        }
        return element;
    }

    private String formatName(String builder, int symbols) {
        if (builder.length() > symbols) {
            return builder.substring(0, symbols) + "...";
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
            psiFiles.remove(currentPsiFile);
            currentPsiFile = JetPsiFactoryUtil.createFile(currentProject, currentPsiFile.getName(), buffer.toString());
            psiFiles.add(currentPsiFile);
            currentDocument = currentPsiFile.getViewProvider().getDocument();
        }
    }

    private int getOffsetFromLineAndChar(int line, int charNumber) {
        int lineStart = currentDocument.getLineStartOffset(line);
        return lineStart + charNumber;
    }

    private List<JetFile> convertList(List<PsiFile> list) {
        List<JetFile> result = new ArrayList<>();
        for (PsiFile file : list) {
            result.add((JetFile) file);
        }
        return result;
    }
}
