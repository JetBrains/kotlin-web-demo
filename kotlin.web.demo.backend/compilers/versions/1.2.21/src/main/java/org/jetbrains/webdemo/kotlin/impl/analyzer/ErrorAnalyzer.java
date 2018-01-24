/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package org.jetbrains.webdemo.kotlin.impl.analyzer;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.diagnostics.Diagnostic;
import org.jetbrains.kotlin.diagnostics.Errors;
import org.jetbrains.kotlin.diagnostics.Severity;
import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.webdemo.kotlin.datastructures.ErrorDescriptor;
import org.jetbrains.webdemo.kotlin.datastructures.TextInterval;
import org.jetbrains.webdemo.kotlin.datastructures.TextPosition;
import org.jetbrains.webdemo.kotlin.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.kotlin.impl.ResolveUtils;
import org.jetbrains.webdemo.kotlin.impl.WrapperSettings;

import java.util.*;

public class ErrorAnalyzer {
    private final List<KtFile> currentPsiFiles;
    private final Project currentProject;

    public ErrorAnalyzer(List<KtFile> currentPsiFiles, Project currentProject) {
        this.currentPsiFiles = currentPsiFiles;
        this.currentProject = currentProject;
    }

    public Map<String, List<ErrorDescriptor>> getAllErrors(boolean isJs) {
        try {
            final Map<String, List<ErrorDescriptor>> errors = new HashMap<>();
            for (PsiFile psiFile : currentPsiFiles) {
                errors.put(psiFile.getName(), getErrorsByVisitor(psiFile));
            }
            BindingContext bindingContext = ResolveUtils.getBindingContext(currentPsiFiles, currentProject, isJs);
            getErrorsFromDiagnostics(bindingContext.getDiagnostics().all(), errors);
            return errors;
        } catch (Throwable e) {
            throw new KotlinCoreException(e);
        }

    }

    public void getErrorsFromDiagnostics(Collection<Diagnostic> diagnostics, Map<String, List<ErrorDescriptor>> errors) {
        try {
            for (Diagnostic diagnostic : diagnostics) {
                //fix for errors in js library files
                VirtualFile virtualFile = diagnostic.getPsiFile().getVirtualFile();
                if (virtualFile == null || virtualFile.getPresentableUrl().startsWith(WrapperSettings.INSTANCE.getJS_LIB_ROOT().toString())) {
                    continue;
                }
                String render = DefaultErrorMessages.render(diagnostic);
                if (render.contains("This cast can never succeed")) {
                    continue;
                }
                if (diagnostic.getSeverity() != Severity.INFO) {
                    Iterator<TextRange> textRangeIterator = diagnostic.getTextRanges().iterator();
                    if (!textRangeIterator.hasNext()) {
                        continue;
                    }
                    TextRange firstRange = textRangeIterator.next();

                    String className = diagnostic.getSeverity().name();
                    if (!(diagnostic.getFactory() == Errors.UNRESOLVED_REFERENCE) && (diagnostic.getSeverity() == Severity.ERROR)) {
                        className = "red_wavy_line";
                    }
                    TextInterval interval = getInterval(firstRange.getStartOffset(), firstRange.getEndOffset(),
                            diagnostic.getPsiFile().getViewProvider().getDocument());
                    errors.get(diagnostic.getPsiFile().getName()).add(
                            new ErrorDescriptor(interval, render, convertSeverity(diagnostic.getSeverity()), className)
                    );
                }
            }

            for (String key : errors.keySet()) {
                Collections.sort(errors.get(key), new Comparator<ErrorDescriptor>() {
                    @Override
                    public int compare(ErrorDescriptor o1, ErrorDescriptor o2) {
                        if (o1.getInterval().getStart().getLine() > o2.getInterval().getStart().getLine()) {
                            return 1;
                        } else if (o1.getInterval().getStart().getLine() < o2.getInterval().getStart().getLine()) {
                            return -1;
                        } else if (o1.getInterval().getStart().getLine() == o2.getInterval().getStart().getLine()) {
                            if (o1.getInterval().getStart().getCh() > o2.getInterval().getStart().getCh()) {
                                return 1;
                            } else if (o1.getInterval().getStart().getCh() < o2.getInterval().getStart().getCh()) {
                                return -1;
                            } else if (o1.getInterval().getStart().getCh() == o2.getInterval().getStart().getCh()) {
                                return 0;
                            }
                        }
                        return -1;
                    }
                });
            }
        } catch (Throwable e) {
            throw new KotlinCoreException(e);
        }
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
            TextInterval interval = getInterval(start, end, psiFile.getViewProvider().getDocument());
            errors.add(new ErrorDescriptor(interval, errorElement.getErrorDescription(),
                    convertSeverity(Severity.ERROR), "red_wavy_line"));
        }
        return errors;
    }

    @NotNull
    private org.jetbrains.webdemo.kotlin.datastructures.Severity convertSeverity(Severity severity) {
        switch (severity) {
            case ERROR:
                return org.jetbrains.webdemo.kotlin.datastructures.Severity.ERROR;
            case INFO:
                return org.jetbrains.webdemo.kotlin.datastructures.Severity.INFO;
            case WARNING:
                return org.jetbrains.webdemo.kotlin.datastructures.Severity.WARNING;
            default:
                return null;
        }
    }

    private TextInterval getInterval(int start, int end, Document currentDocument) {
        int lineNumberForElementStart = currentDocument.getLineNumber(start);
        int lineNumberForElementEnd = currentDocument.getLineNumber(end);
        int charNumberForElementStart = start - currentDocument.getLineStartOffset(lineNumberForElementStart);
        int charNumberForElementEnd = end - currentDocument.getLineStartOffset(lineNumberForElementStart);
        if ((start == end) && (lineNumberForElementStart == lineNumberForElementEnd)) {
            charNumberForElementStart--;
            if (charNumberForElementStart < 0) {
                charNumberForElementStart++;
                charNumberForElementEnd++;
            }
        }
        TextPosition startPosition = new TextPosition(lineNumberForElementStart, charNumberForElementStart);
        TextPosition endPosition = new TextPosition(lineNumberForElementEnd, charNumberForElementEnd);
        return new TextInterval(startPosition, endPosition);
    }
}
