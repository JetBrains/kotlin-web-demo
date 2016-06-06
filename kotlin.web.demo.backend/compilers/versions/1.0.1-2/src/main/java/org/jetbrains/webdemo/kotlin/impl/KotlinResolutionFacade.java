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

package org.jetbrains.webdemo.kotlin.impl;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.analyzer.AnalysisResult;
import org.jetbrains.kotlin.container.ComponentProvider;
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor;
import org.jetbrains.kotlin.descriptors.ModuleDescriptor;
import org.jetbrains.kotlin.idea.resolve.ResolutionFacade;
import org.jetbrains.kotlin.psi.KtDeclaration;
import org.jetbrains.kotlin.psi.KtElement;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.kotlin.resolve.lazy.BodyResolveMode;
import org.jetbrains.webdemo.kotlin.impl.environment.EnvironmentManager;

import java.util.Collection;

public class KotlinResolutionFacade implements ResolutionFacade {
    private final ComponentProvider provider;

    public KotlinResolutionFacade(ComponentProvider provider) {
        this.provider = provider;
    }

    @NotNull
    @Override
    public Project getProject() {
        return EnvironmentManager.getEnvironment().getProject();
    }

    @NotNull
    @Override
    public BindingContext analyze(@NotNull KtElement jetElement, @NotNull BodyResolveMode bodyResolveMode) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public AnalysisResult analyzeFullyAndGetResult(@NotNull Collection<? extends KtElement> collection) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public DeclarationDescriptor resolveToDescriptor(@NotNull KtDeclaration jetDeclaration) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public ModuleDescriptor getModuleDescriptor() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public <T> T getFrontendService(@NotNull Class<T> aClass) {
        if (provider == null) return null;
        return (T) provider.resolve(aClass).getValue();
    }

    @NotNull
    @Override
    public <T> T getIdeService(@NotNull Class<T> aClass) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public <T> T getFrontendService(@NotNull PsiElement psiElement, @NotNull Class<T> aClass) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    @Override
    public <T> T getFrontendService(@NotNull ModuleDescriptor moduleDescriptor, @NotNull Class<T> aClass) {
        throw new UnsupportedOperationException();
    }
}
