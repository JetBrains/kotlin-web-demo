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
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.k2js.facade.WebDemoTranslatorFacade;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.Initializer;
import org.jetbrains.webdemo.Interval;
import org.jetbrains.webdemo.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.jet.lang.cfg.pseudocode.JetControlFlowDataTraceFactory;
import org.jetbrains.jet.lang.diagnostics.*;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.java.AnalyzerFacadeForJVM;


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
    private final Project currentProject;

    private final SessionInfo sessionInfo;

    public ErrorAnalyzer(PsiFile currentPsiFile, SessionInfo info) {
        this.currentPsiFile = currentPsiFile;
        this.currentProject = currentPsiFile.getProject();
        this.currentDocument = currentPsiFile.getViewProvider().getDocument();
        this.sessionInfo = info;
    }

    private void gerErrorsFromBindingContext(BindingContext bindingContext, List<ErrorDescriptor> errors) {
        Collection<Diagnostic> diagnostics = bindingContext.getDiagnostics();
        try {
            for (Diagnostic diagnostic : diagnostics) {
                if (diagnostic instanceof DiagnosticWithPsiElement && diagnostic.getPsiFile().getName().contains("core")) {
                    continue;
                }
                if (diagnostic.getMessage().contains("This cast can never succeed")) {
                    continue;
                }
                if (diagnostic.getSeverity() != Severity.INFO) {
                    //TODO
                    TextRange firstRange = (TextRange) diagnostic.getTextRanges().iterator().next();
                    String className = diagnostic.getSeverity().name();
                    if (!(diagnostic instanceof UnresolvedReferenceDiagnostic) && (diagnostic.getSeverity() == Severity.ERROR)) {
                        className = "red_wavy_line";
                    }
                    errors.add(new ErrorDescriptor(new Interval(firstRange.getStartOffset(), firstRange.getEndOffset(), currentDocument),
                            diagnostic.getMessage(), diagnostic.getSeverity(), className));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
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

    public List<ErrorDescriptor> getAllErrors() {

        final List<ErrorDescriptor> errors = getErrorsByVisitor();
        sessionInfo.getTimeManager().saveCurrentTime();
        BindingContext bindingContext;
        try {
            if (sessionInfo.getRunConfiguration().equals(SessionInfo.RunConfiguration.JAVA)) {
                bindingContext = AnalyzerFacadeForJVM.analyzeOneFileWithJavaIntegration(
                        (JetFile) currentPsiFile, JetControlFlowDataTraceFactory.EMPTY);
            } else {
                bindingContext = WebDemoTranslatorFacade.analyzeProgramCode(Initializer.INITIALIZER.getEnvironment().getProject(), (JetFile) currentPsiFile);
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
