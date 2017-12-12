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

package org.jetbrains.webdemo.kotlin.impl.translator;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment;
import org.jetbrains.kotlin.config.CommonConfigurationKeys;
import org.jetbrains.kotlin.config.CompilerConfiguration;
import org.jetbrains.kotlin.js.config.JSConfigurationKeys;
import org.jetbrains.kotlin.js.config.JsConfig;
import org.jetbrains.kotlin.js.facade.K2JSTranslator;
import org.jetbrains.kotlin.js.facade.MainCallParameters;
import org.jetbrains.kotlin.js.facade.TranslationResult;
import org.jetbrains.kotlin.js.facade.exceptions.TranslationException;
import org.jetbrains.kotlin.psi.KtFile;
import org.jetbrains.webdemo.kotlin.datastructures.ErrorDescriptor;
import org.jetbrains.webdemo.kotlin.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.kotlin.impl.WrapperSettings;
import org.jetbrains.webdemo.kotlin.impl.analyzer.ErrorAnalyzer;
import org.jetbrains.webdemo.kotlin.impl.environment.EnvironmentManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.ERROR;
import static org.jetbrains.kotlin.cli.common.messages.CompilerMessageSeverity.STRONG_WARNING;

@SuppressWarnings("UnusedDeclaration")
public final class WebDemoTranslatorFacade {

    public static final List<String> LIBRARY_FILES = Collections.singletonList(WrapperSettings.JS_LIB_ROOT.toString());

    @SuppressWarnings("FieldCanBeLocal")
    private static String EXCEPTION = "exception=";

    private WebDemoTranslatorFacade() {
    }

    public static org.jetbrains.webdemo.kotlin.datastructures.TranslationResult translateProjectWithCallToMain
            (@NotNull List<KtFile> files, @NotNull String[] arguments) {
        try {
            return doTranslate(files, arguments);
        } catch (Throwable e) {
            throw new KotlinCoreException(e);
        } finally {
            EnvironmentManager.reinitializeJavaEnvironment();
        }
    }

    @NotNull
    private static org.jetbrains.webdemo.kotlin.datastructures.TranslationResult doTranslate(
            @NotNull List<KtFile> files,
            @NotNull String[] arguments
    ) throws TranslationException {
        KotlinCoreEnvironment environment = EnvironmentManager.getEnvironment();
        Project currentProject = environment.getProject();

        CompilerConfiguration configuration = environment.getConfiguration().copy();
        configuration.put(CommonConfigurationKeys.MODULE_NAME, "moduleId");
        configuration.put(JSConfigurationKeys.LIBRARIES, Collections.singletonList(WrapperSettings.JS_LIB_ROOT.toString()));

        JsConfig config = new JsConfig(currentProject, configuration);
        JsConfig.Reporter reporter = new JsConfig.Reporter() {
            @Override
            public void error(@NotNull String message) {
            }

            @Override
            public void warning(@NotNull String message) {
            }
        };
        K2JSTranslator translator = new K2JSTranslator(config);
        TranslationResult result = translator.translate(
                reporter,
                files,
                MainCallParameters.mainWithArguments(Arrays.asList(arguments)));
        if (result instanceof TranslationResult.Success) {
            TranslationResult.Success success = ((TranslationResult.Success) result);

            return new org.jetbrains.webdemo.kotlin.datastructures.TranslationResult(
                    "kotlin.kotlin.io.output.flush();\n" + success.getCode() + "\n" + "kotlin.kotlin.io.output.buffer;\n");

        } else {
            Map<String, List<ErrorDescriptor>> errors = new HashMap<>();
            for (PsiFile psiFile : files) {
                errors.put(psiFile.getName(), new ArrayList<ErrorDescriptor>());
            }
            ArrayList<ErrorDescriptor> errorDescriptors = new ArrayList<ErrorDescriptor>();
            ErrorAnalyzer errorAnalyzer = new ErrorAnalyzer(files, currentProject);
            errorAnalyzer.getErrorsFromDiagnostics(result.getDiagnostics().all(), errors);
            return new org.jetbrains.webdemo.kotlin.datastructures.TranslationResult(errors);
        }
    }

}