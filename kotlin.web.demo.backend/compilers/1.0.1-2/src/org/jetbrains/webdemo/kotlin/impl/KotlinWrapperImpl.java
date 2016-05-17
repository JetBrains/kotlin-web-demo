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


import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.webdemo.Project;
import org.jetbrains.webdemo.ProjectFile;
import org.jetbrains.webdemo.kotlin.datastructures.CompletionVariant;
import org.jetbrains.webdemo.kotlin.datastructures.ErrorDescriptor;
import org.jetbrains.webdemo.kotlin.impl.analyzer.ErrorAnalyzer;
import org.jetbrains.webdemo.kotlin.impl.completion.CompletionProvider;
import org.jetbrains.webdemo.kotlin.impl.converter.WebDemoJavaToKotlinConverter;
import org.jetbrains.webdemo.kotlin.impl.environment.EnvironmentManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KotlinWrapperImpl implements org.jetbrains.webdemo.kotlin.KotlinWrapper {
    @Override
    public void init() {
        EnvironmentManager.init();
    }

    public String translateJavaToKotlin(String javaCode) {
       return WebDemoJavaToKotlinConverter.getResult(javaCode);
    }

    @Override
    public Map<String, List<ErrorDescriptor>> getErrors(Project project) {
        List<KtFile> files = createPsiFiles(project);
        ErrorAnalyzer analyzer = new ErrorAnalyzer(files, EnvironmentManager.getEnvironment().getProject());
        boolean isJs = project.confType.equals("js") || project.confType.equals("canvas");
        return analyzer.getAllErrors(isJs);
    }

    @Override
    public List<CompletionVariant> getCompletionVariants(Project project, String filename, int line, int ch) {
        List<KtFile> files = createPsiFiles(project);
        CompletionProvider completionProvider = new CompletionProvider(files, filename, line, ch);
        boolean isJs = project.confType.equals("js") || project.confType.equals("canvas");
        return completionProvider.getResult(isJs);
    }

    private List<KtFile> createPsiFiles(Project project) {
        List<KtFile> result = new ArrayList<>();
        for (ProjectFile file : project.files) {
            result.add(JetPsiFactoryUtil.createFile(EnvironmentManager.getEnvironment().getProject(), file.getName(), file.getText()));
        }
        return result;
    }
}
