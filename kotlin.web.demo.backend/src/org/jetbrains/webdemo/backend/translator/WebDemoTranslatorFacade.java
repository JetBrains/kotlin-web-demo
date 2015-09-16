/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package org.jetbrains.webdemo.backend.translator;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.js.analyze.TopDownAnalyzerFacadeForJS;
import org.jetbrains.kotlin.js.config.Config;
import org.jetbrains.kotlin.js.config.LibrarySourcesConfig;
import org.jetbrains.kotlin.js.facade.K2JSTranslator;
import org.jetbrains.kotlin.js.facade.MainCallParameters;
import org.jetbrains.kotlin.js.facade.TranslationResult;
import org.jetbrains.kotlin.js.facade.exceptions.TranslationException;
import org.jetbrains.kotlin.psi.JetFile;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.backend.BackendSessionInfo;
import org.jetbrains.webdemo.backend.BackendSettings;
import org.jetbrains.webdemo.backend.Initializer;
import org.jetbrains.webdemo.backend.errorsDescriptors.ErrorAnalyzer;
import org.jetbrains.webdemo.backend.errorsDescriptors.ErrorDescriptor;
import org.jetbrains.webdemo.backend.exceptions.KotlinCoreException;

import java.io.File;
import java.util.*;

@SuppressWarnings("UnusedDeclaration")
public final class WebDemoTranslatorFacade {

    public static final String JS_LIB_ROOT = new File(BackendSettings.WEBAPP_ROOT_DIRECTORY + File.separator + "js").getAbsolutePath();
    public static final List<String> LIBRARY_FILES = Collections.singletonList(JS_LIB_ROOT);

    @SuppressWarnings("FieldCanBeLocal")
    private static String EXCEPTION = "exception=";

    private WebDemoTranslatorFacade() {
    }

    @SuppressWarnings("UnusedDeclaration")
    @Nullable
    public static BindingContext analyzeProgramCode(@NotNull List<JetFile> files, BackendSessionInfo sessionInfo) {
        try {
            return TopDownAnalyzerFacadeForJS.analyzeFiles(files, new LibrarySourcesConfig.Builder(
                    Initializer.getInstance().getEnvironment().getProject(),
                    "moduleId",
                    LIBRARY_FILES
            ).build()).getBindingContext();
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    BackendSessionInfo.TypeOfRequest.CONVERT_TO_JS.name(), sessionInfo.getOriginUrl(), "");
            return null;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    public static ObjectNode translateProjectWithCallToMain(@NotNull List<JetFile> files,
                                                            @NotNull String arguments,
                                                            BackendSessionInfo sessionInfo,
                                                            Map<String, List<ErrorDescriptor>> errors) {
        try {
            MyTranslationResult translationResult = doTranslate(files, arguments, sessionInfo, errors);
            if (translationResult.success) {
                ObjectNode result = new ObjectNode(JsonNodeFactory.instance);
                result.put("type", "generatedJSCode");
                result.put("text", translationResult.jsCode);
                return result;
            } else {
                return null;
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    BackendSessionInfo.TypeOfRequest.CONVERT_TO_JS.name(), sessionInfo.getOriginUrl(), "");
            throw new KotlinCoreException(e);
        } finally {
            Initializer.reinitializeJavaEnvironment();
        }
    }

    @NotNull
    private static MyTranslationResult doTranslate(@NotNull List<JetFile> files,
                                                   @NotNull String arguments,
                                                   BackendSessionInfo sessionInfo,
                                                   Map<String, List<ErrorDescriptor>> errors) throws TranslationException {
        Project currentProject = Initializer.getInstance().getEnvironment().getProject();
        Config config = new LibrarySourcesConfig.Builder(
                currentProject,
                "moduleId",
                LIBRARY_FILES
        ).build();
        K2JSTranslator translator = new K2JSTranslator(config);
        TranslationResult result = translator.translate(files, MainCallParameters.mainWithArguments(Arrays.asList(ResponseUtils.splitArguments(arguments))));
        if (result instanceof org.jetbrains.kotlin.js.facade.TranslationResult.Success) {
            org.jetbrains.kotlin.js.facade.TranslationResult.Success success = ((org.jetbrains.kotlin.js.facade.TranslationResult.Success) result);
            return new MyTranslationResult(K2JSTranslator.FLUSH_SYSTEM_OUT + success.getCode() + "\n" + K2JSTranslator.GET_SYSTEM_OUT);
        } else {
            for (PsiFile psiFile : files) {
                errors.put(psiFile.getName(), new ArrayList<ErrorDescriptor>());
            }
            ArrayList<ErrorDescriptor> errorDescriptors = new ArrayList<ErrorDescriptor>();
            ErrorAnalyzer errorAnalyzer = new ErrorAnalyzer((List) files, sessionInfo, currentProject);
            errorAnalyzer.getErrorsFromDiagnostics(result.getDiagnostics().all(), errors);
            return new MyTranslationResult(errors);
        }
    }

}

class MyTranslationResult {
    boolean success;
    String jsCode;
    Map<String, List<ErrorDescriptor>> errors;

    MyTranslationResult(String jsCode) {
        this.jsCode = jsCode;
        success = true;
    }

    MyTranslationResult(Map<String, List<ErrorDescriptor>> errors) {
        this.errors = errors;
        success = false;
    }
}
