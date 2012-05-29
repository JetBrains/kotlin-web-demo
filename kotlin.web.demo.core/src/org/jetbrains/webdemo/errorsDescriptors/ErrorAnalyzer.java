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

import org.jetbrains.jet.internal.com.intellij.openapi.editor.Document;
import org.jetbrains.jet.internal.com.intellij.openapi.util.TextRange;
import org.jetbrains.jet.internal.com.intellij.psi.PsiElement;
import org.jetbrains.jet.internal.com.intellij.psi.PsiElementVisitor;
import org.jetbrains.jet.internal.com.intellij.psi.PsiErrorElement;
import org.jetbrains.jet.internal.com.intellij.psi.PsiFile;
import org.jetbrains.jet.lang.diagnostics.Diagnostic;
import org.jetbrains.jet.lang.diagnostics.Errors;
import org.jetbrains.jet.lang.diagnostics.Severity;
import org.jetbrains.jet.lang.diagnostics.rendering.DefaultErrorMessages;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.java.AnalyzerFacadeForJVM;
import org.jetbrains.jet.lang.resolve.java.CompilerDependencies;
import org.jetbrains.jet.lang.resolve.java.CompilerSpecialMode;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.Interval;
import org.jetbrains.webdemo.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.translator.WebDemoTranslatorFacade;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/28/11
 * Time: 11:24 AM
 */

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
                bindingContext = AnalyzerFacadeForJVM.analyzeOneFileWithJavaIntegration(
                        (JetFile) currentPsiFile,
                        CompilerDependencies.compilerDependenciesForProduction(ApplicationSettings.MODE)).getBindingContext();
            }
            else {
                bindingContext = WebDemoTranslatorFacade.analyzeProgramCode((JetFile) currentPsiFile);
            }

        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), currentPsiFile.getText());
            throw new KotlinCoreException(e);
        }
        String info = ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(), sessionInfo.getId(),
                "ANALYZE namespaces " + sessionInfo.getTimeManager().getMillisecondsFromSavedTime() + " size: " + currentPsiFile.getTextLength());
        ErrorWriter.ERROR_WRITER.writeInfo(info);
        if (bindingContext != null) {
            gerErrorsFromBindingContext(bindingContext, errors);
        }
        return errors;
    }

    private void gerErrorsFromBindingContext(BindingContext bindingContext, List<ErrorDescriptor> errors) {
        Collection<Diagnostic> diagnostics = bindingContext.getDiagnostics();
        try {
            for (Diagnostic diagnostic : diagnostics) {
                //fix for errors in js library files
                if (diagnostic.getPsiFile().getName().contains("core")) {
                    continue;
                }
                String render = DefaultErrorMessages.RENDERER.render(diagnostic);
                if (render.contains("This cast can never succeed")) {
                    continue;
                }
                if (diagnostic.getSeverity() != Severity.INFO) {
                    Iterator<TextRange> textRangeIterator = diagnostic.getTextRanges().iterator();
                    if (textRangeIterator == null) {
                        ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer("Text range iterator is null",
                                diagnostic.getTextRanges() + " " + render,
                                SessionInfo.TypeOfRequest.HIGHLIGHT.name(), currentPsiFile.getText());
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
                    SessionInfo.TypeOfRequest.HIGHLIGHT.name(), currentPsiFile.getText());
        }

        Collections.sort(errors, new Comparator<ErrorDescriptor>() {
            @Override
            public int compare(ErrorDescriptor o1, ErrorDescriptor o2) {
                if (o1.getInterval().startPoint.line > o2.getInterval().startPoint.line) {
                    return 1;
                }
                else if (o1.getInterval().startPoint.line < o2.getInterval().startPoint.line) {
                    return -1;
                }
                else if (o1.getInterval().startPoint.line == o2.getInterval().startPoint.line) {
                    if (o1.getInterval().startPoint.charNumber > o2.getInterval().startPoint.charNumber) {
                        return 1;
                    }
                    else if (o1.getInterval().startPoint.charNumber < o2.getInterval().startPoint.charNumber) {
                        return -1;
                    }
                    else if (o1.getInterval().startPoint.charNumber == o2.getInterval().startPoint.charNumber) {
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
