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

package org.jetbrains.webdemo.errorsDescriptors;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.kotlin.diagnostics.Diagnostic;
import org.jetbrains.kotlin.diagnostics.Errors;
import org.jetbrains.kotlin.diagnostics.Severity;
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages;
import org.jetbrains.kotlin.psi.JetFile;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.diagnostics.Diagnostics;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.Interval;
import org.jetbrains.webdemo.ResolveUtils;
import org.jetbrains.webdemo.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.translator.WebDemoTranslatorFacade;

import java.util.*;

public class ErrorAnalyzer {
    private final PsiFile currentPsiFile;
    private final Document currentDocument;

    private final SessionInfo sessionInfo;

    public ErrorAnalyzer(PsiFile currentPsiFile, SessionInfo info) {
        this.currentPsiFile = currentPsiFile;
        this.currentDocument = currentPsiFile.getViewProvider().getDocument();
        this.sessionInfo = info;
    }

    public List<ErrorDescriptor> getAllErrors() {
        final List<ErrorDescriptor> errors = getErrorsByVisitor();
        sessionInfo.getTimeManager().saveCurrentTime();
        BindingContext bindingContext;
        try {
            if (sessionInfo.getRunConfiguration().equals(SessionInfo.RunConfiguration.JAVA)) {
                bindingContext = ResolveUtils.getBindingContext((JetFile) currentPsiFile);
            }
            else {
                bindingContext = WebDemoTranslatorFacade.analyzeProgramCode((JetFile) currentPsiFile, sessionInfo);
            }

        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
            throw new KotlinCoreException(e);
        }
        String info = ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(), sessionInfo.getId(),
                "ANALYZE namespaces " + sessionInfo.getTimeManager().getMillisecondsFromSavedTime() + " size: " + currentPsiFile.getTextLength());
        ErrorWriter.ERROR_WRITER.writeInfo(info);
        if (bindingContext != null) {
            processDiagnostics(bindingContext.getDiagnostics(), errors);
        }
        return errors;
    }

    public void processDiagnostics(Diagnostics diagnostics, List<ErrorDescriptor> errors) {
        try {
            for (Diagnostic diagnostic : diagnostics) {
                //fix for errors in js library files
                VirtualFile virtualFile = diagnostic.getPsiFile().getVirtualFile();
                if (virtualFile == null || virtualFile.getPresentableUrl().startsWith(WebDemoTranslatorFacade.JS_LIB_ROOT)) {
                    continue;
                }

                String render = DefaultErrorMessages.render(diagnostic);
                if (render.contains("This cast can never succeed")) {
                    continue;
                }
                if (diagnostic.getSeverity() != Severity.INFO) {
                    Iterator<TextRange> textRangeIterator = diagnostic.getTextRanges().iterator();
                    if (textRangeIterator == null) {
                        ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer("Text range iterator is null",
                                diagnostic.getTextRanges() + " " + render,
                                SessionInfo.TypeOfRequest.HIGHLIGHT.name(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
                        continue;
                    }

                    if (!textRangeIterator.hasNext()) {
                        /*ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer("Text range for diagnostic is empty.",
                                "diagnostic.getTextRanges(): " + diagnostic.getTextRanges() + "\nDefaultErrorMessages.RENDERER.render(diagnostic): " + render,
                                SessionInfo.TypeOfRequest.HIGHLIGHT.name(), currentPsiFile.getText());*/
                        continue;
                    }
                    TextRange firstRange = textRangeIterator.next();

                    String className = diagnostic.getSeverity().name();
                    if (!(diagnostic.getFactory() == Errors.UNRESOLVED_REFERENCE) && (diagnostic.getSeverity() == Severity.ERROR)) {
                        className = "red_wavy_line";
                    }
                    errors.add(new ErrorDescriptor(new Interval(firstRange.getStartOffset(), firstRange.getEndOffset(), currentDocument),
                            render, diagnostic.getSeverity(), className));
                }
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.HIGHLIGHT.name(),sessionInfo.getOriginUrl() , currentPsiFile.getText());
        }

        Collections.sort(errors, new Comparator<ErrorDescriptor>() {
            @Override
            public int compare(ErrorDescriptor o1, ErrorDescriptor o2) {
                if (o1.getInterval().startPoint.line > o2.getInterval().startPoint.line) {
                    return 1;
                } else if (o1.getInterval().startPoint.line < o2.getInterval().startPoint.line) {
                    return -1;
                } else if (o1.getInterval().startPoint.line == o2.getInterval().startPoint.line) {
                    if (o1.getInterval().startPoint.charNumber > o2.getInterval().startPoint.charNumber) {
                        return 1;
                    } else if (o1.getInterval().startPoint.charNumber < o2.getInterval().startPoint.charNumber) {
                        return -1;
                    } else if (o1.getInterval().startPoint.charNumber == o2.getInterval().startPoint.charNumber) {
                        return 0;
                    }
                }
                return -1;
            }
        });
    }

    private List<ErrorDescriptor> getErrorsByVisitor() {
        final List<PsiErrorElement> errorElements = new ArrayList<PsiErrorElement>();
        PsiElementVisitor visitor = new PsiElementVisitor() {
            @Override
            public void visitElement(PsiElement element) {
                element.acceptChildren(this);
            }

            @Override
            public void visitErrorElement(PsiErrorElement element) {
                errorElements.add(element);
            }

        };

        visitor.visitFile(currentPsiFile);
        final List<ErrorDescriptor> errors = new ArrayList<ErrorDescriptor>();
        for (PsiErrorElement errorElement : errorElements) {
            int start = errorElement.getTextRange().getStartOffset();
            int end = errorElement.getTextRange().getEndOffset();
            Interval interval = new Interval(start, end, currentDocument);
            errors.add(new ErrorDescriptor(interval, errorElement.getErrorDescription(), Severity.ERROR, "red_wavy_line"));
        }
        return errors;
    }
}
