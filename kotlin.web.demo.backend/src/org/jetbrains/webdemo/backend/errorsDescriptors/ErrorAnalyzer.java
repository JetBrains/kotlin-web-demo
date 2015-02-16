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

package org.jetbrains.webdemo.backend.errorsDescriptors;

import com.intellij.openapi.project.Project;
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
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.backend.Interval;
import org.jetbrains.webdemo.backend.ResolveUtils;
import org.jetbrains.webdemo.backend.SessionInfo;
import org.jetbrains.webdemo.backend.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.backend.translator.WebDemoTranslatorFacade;

import java.util.*;

public class ErrorAnalyzer {
    private final List<PsiFile> currentPsiFiles;
    //    private final Document currentDocument;
    private final Project currentProject;

    private final SessionInfo sessionInfo;

    public ErrorAnalyzer(List<PsiFile> currentPsiFiles, SessionInfo info, Project currentProject) {
        this.currentPsiFiles = currentPsiFiles;
        this.currentProject = currentProject;
//        this.currentDocument = currentPsiFiles.getViewProvider().getDocument();
        this.sessionInfo = info;
    }

    public Map<String, List<ErrorDescriptor>> getAllErrors() {
        final Map<String, List<ErrorDescriptor>> errors = new HashMap<>();
        for(PsiFile psiFile : currentPsiFiles){
            errors.put(psiFile.getName(), getErrorsByVisitor(psiFile));
        }
        sessionInfo.getTimeManager().saveCurrentTime();
        BindingContext bindingContext;
        try {
            if (sessionInfo.getRunConfiguration().equals(SessionInfo.RunConfiguration.JAVA) ||
                    sessionInfo.getRunConfiguration().equals(SessionInfo.RunConfiguration.JUNIT)) {
                bindingContext = ResolveUtils.getBindingContext(convertList(currentPsiFiles), currentProject);
            } else {
                bindingContext = WebDemoTranslatorFacade.analyzeProgramCode(convertList(currentPsiFiles), sessionInfo);
            }

        } catch (Throwable e) {
//            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), currentPsiFiles);
            throw new KotlinCoreException(e);
        }
//        String info = ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(), sessionInfo.getId(),
//                "ANALYZE namespaces " + sessionInfo.getTimeManager().getMillisecondsFromSavedTime() + " size: " + currentPsiFile.getTextLength());
//        ErrorWriter.ERROR_WRITER.writeInfo(info);
        if (bindingContext != null) {
            getErrorsFromBindingContext(bindingContext, errors);
        }
        return errors;
    }

    public void getErrorsFromBindingContext(BindingContext bindingContext, Map<String, List<ErrorDescriptor>> errors) {
        Collection<Diagnostic> diagnostics = bindingContext.getDiagnostics().all();
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
//                        ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer("Text range iterator is null",
//                                diagnostic.getTextRanges() + " " + render,
//                                SessionInfo.TypeOfRequest.HIGHLIGHT.name(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
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
                    errors.get(diagnostic.getPsiFile().getName()).add(new ErrorDescriptor(new Interval(firstRange.getStartOffset(), firstRange.getEndOffset(), diagnostic.getPsiFile().getViewProvider().getDocument()),
                            render, diagnostic.getSeverity(), className));
                }
            }
        } catch (Throwable e) {
//            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
//                    SessionInfo.TypeOfRequest.HIGHLIGHT.name(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
        }

        for (String key : errors.keySet())
            Collections.sort(errors.get(key), new Comparator<ErrorDescriptor>() {
                @Override
                public int compare(ErrorDescriptor o1, ErrorDescriptor o2) {
                    if (o1.getInterval().start.line > o2.getInterval().start.line) {
                        return 1;
                    } else if (o1.getInterval().start.line < o2.getInterval().start.line) {
                        return -1;
                    } else if (o1.getInterval().start.line == o2.getInterval().start.line) {
                        if (o1.getInterval().start.ch > o2.getInterval().start.ch) {
                            return 1;
                        } else if (o1.getInterval().start.ch < o2.getInterval().start.ch) {
                            return -1;
                        } else if (o1.getInterval().start.ch == o2.getInterval().start.ch) {
                            return 0;
                        }
                    }
                    return -1;
                }
            });

    }

    private List<JetFile> convertList(List<PsiFile> list) {
        List<JetFile> result = new ArrayList<>();
        for(PsiFile file : list){
            result.add((JetFile)file);
        }
        return result;
    }


    private List<ErrorDescriptor> getErrorsByVisitor(PsiFile psiFile) {
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

        final List<ErrorDescriptor> errors = new ArrayList<>();
        visitor.visitFile(psiFile);
        for (PsiErrorElement errorElement : errorElements) {
            int start = errorElement.getTextRange().getStartOffset();
            int end = errorElement.getTextRange().getEndOffset();
            Interval interval = new Interval(start, end, psiFile.getViewProvider().getDocument());
            errors.add(new ErrorDescriptor(interval, errorElement.getErrorDescription(), Severity.ERROR, "red_wavy_line"));
        }
        return errors;
    }
}
