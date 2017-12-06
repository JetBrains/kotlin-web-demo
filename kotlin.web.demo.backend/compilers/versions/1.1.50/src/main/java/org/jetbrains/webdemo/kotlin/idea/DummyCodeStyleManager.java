/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package org.jetbrains.webdemo.kotlin.idea;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.ChangedRangesInfo;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.Indent;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ThrowableRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class DummyCodeStyleManager extends CodeStyleManager {
    @NotNull
    @Override
    public Project getProject() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public PsiElement reformat(@NotNull PsiElement psiElement) throws IncorrectOperationException {
        return psiElement;
    }

    @NotNull
    @Override
    public PsiElement reformat(@NotNull PsiElement psiElement, boolean b) throws IncorrectOperationException {
        return psiElement;
    }

    @Override
    public PsiElement reformatRange(@NotNull PsiElement psiElement, int i, int i1) throws IncorrectOperationException {
        return psiElement;
    }

    @Override
    public PsiElement reformatRange(@NotNull PsiElement psiElement, int i, int i1, boolean b) throws IncorrectOperationException {
        return psiElement;
    }

    @Override
    public void reformatText(@NotNull PsiFile psiFile, int i, int i1) throws IncorrectOperationException {

    }

    @Override
    public void reformatText(@NotNull PsiFile psiFile, @NotNull Collection<TextRange> collection) throws IncorrectOperationException {

    }

    @Override
    public void reformatTextWithContext(@NotNull PsiFile psiFile, @NotNull ChangedRangesInfo changedRangesInfo) throws IncorrectOperationException {

    }

    @Override
    public void reformatTextWithContext(@NotNull PsiFile psiFile, @NotNull Collection<TextRange> collection) throws IncorrectOperationException {

    }

    @Override
    public void adjustLineIndent(@NotNull PsiFile psiFile, TextRange textRange) throws IncorrectOperationException {

    }

    @Override
    public int adjustLineIndent(@NotNull PsiFile psiFile, int i) throws IncorrectOperationException {
        return i;
    }

    @Override
    public int adjustLineIndent(@NotNull Document document, int i) {
        return i;
    }

    @Override
    public boolean isLineToBeIndented(@NotNull PsiFile psiFile, int i) {
        return false;
    }

    @Nullable
    @Override
    public String getLineIndent(@NotNull PsiFile psiFile, int i) {
        return null;
    }

    @Nullable
    @Override
    public String getLineIndent(@NotNull Document document, int i) {
        return null;
    }

    @Override
    public Indent getIndent(String s, FileType fileType) {
        return null;
    }

    @Override
    public String fillIndent(Indent indent, FileType fileType) {
        return null;
    }

    @Override
    public Indent zeroIndent() {
        return null;
    }

    @Override
    public void reformatNewlyAddedElement(@NotNull ASTNode astNode, @NotNull ASTNode astNode1) throws IncorrectOperationException {

    }

    @Override
    public boolean isSequentialProcessingAllowed() {
        return false;
    }

    @Override
    public void performActionWithFormatterDisabled(Runnable runnable) {
        runnable.run();
    }

    @Override
    public <T extends Throwable> void performActionWithFormatterDisabled(ThrowableRunnable<T> throwableRunnable) throws T {
       throwableRunnable.run();
    }

    @Override
    public <T> T performActionWithFormatterDisabled(Computable<T> computable) {
        return computable.compute();
    }
}
