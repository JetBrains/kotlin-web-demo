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

package org.jetbrains.webdemo.kotlin.impl.compiler;

import com.intellij.openapi.project.Project;
import org.jetbrains.kotlin.backend.common.output.OutputFile;
import org.jetbrains.kotlin.codegen.ClassFileFactory;
import org.jetbrains.kotlin.codegen.CompilationErrorHandler;
import org.jetbrains.kotlin.codegen.KotlinCodegenFacade;
import org.jetbrains.kotlin.codegen.state.GenerationState;
import org.jetbrains.kotlin.config.CompilerConfiguration;
import org.jetbrains.kotlin.fileClasses.NoResolveFileClassesProvider;
import org.jetbrains.kotlin.idea.MainFunctionDetector;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.webdemo.kotlin.datastructures.CompilationResult;
import org.jetbrains.webdemo.kotlin.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.kotlin.impl.ResolveUtils;
import org.jetbrains.webdemo.kotlin.impl.WrapperLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KotlinCompilerWrapper {
    public CompilationResult compile(List<KtFile> currentPsiFiles, Project currentProject, CompilerConfiguration configuration, String fileName) {
        try {
            GenerationState generationState = ResolveUtils.getGenerationState(currentPsiFiles, currentProject, configuration);
            String mainClass = findMainClass(generationState.getBindingContext(), currentPsiFiles, fileName);
            KotlinCodegenFacade.compileCorrectFiles(generationState, new CompilationErrorHandler() {
                @Override
                public void reportException(Throwable throwable, String fileUrl) {
                    WrapperLogger.reportException("Compilation error at file " + fileUrl, throwable);
                }
            });

            final ClassFileFactory factory = generationState.getFactory();
            Map<String, byte[]> files = new HashMap<>();
            for (OutputFile file : factory.asList()) {
                files.put(file.getRelativePath(), file.asByteArray());
            }
            return new CompilationResult(files, mainClass);
        } catch (Throwable e) {
            throw new KotlinCoreException(e);
        }
    }

    private String findMainClass(BindingContext bindingContext, List<KtFile> files, String fileName) {
        MainFunctionDetector mainFunctionDetector = new MainFunctionDetector(bindingContext);
        for (KtFile file : files) {
            if (file.getName().contains(fileName) && mainFunctionDetector.hasMain(file.getDeclarations())) {
                return getMainClassName(file);
            }
        }
        for (KtFile file : files) {
            if (mainFunctionDetector.hasMain(file.getDeclarations())) {
                return getMainClassName(file);
            }
        }
        return getMainClassName(files.iterator().next());
    }

    private String getMainClassName(KtFile file) {
        return NoResolveFileClassesProvider.INSTANCE.getFileClassInfo(file).getFileClassFqName().asString();
    }
}
