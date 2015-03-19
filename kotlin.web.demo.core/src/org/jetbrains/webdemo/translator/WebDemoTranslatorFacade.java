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

package org.jetbrains.webdemo.translator;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.kotlin.js.analyze.TopDownAnalyzerFacadeForJS;
import org.jetbrains.kotlin.js.config.EcmaVersion;
import org.jetbrains.kotlin.js.config.LibrarySourcesConfig;
import org.jetbrains.kotlin.js.facade.K2JSTranslator;
import org.jetbrains.kotlin.js.facade.MainCallParameters;
import org.jetbrains.kotlin.js.facade.exceptions.MainFunctionNotFoundException;
import org.jetbrains.kotlin.js.facade.exceptions.TranslationException;
import org.jetbrains.kotlin.psi.JetFile;
import org.jetbrains.kotlin.resolve.BindingContext;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.Initializer;
import org.jetbrains.webdemo.JetPsiFactoryUtil;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.errorsDescriptors.ErrorAnalyzer;
import org.jetbrains.webdemo.errorsDescriptors.ErrorDescriptor;
import org.jetbrains.webdemo.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.responseHelpers.JsonResponseForHighlighting;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.json.JSONArray;

import java.io.File;
import java.util.*;

@SuppressWarnings("UnusedDeclaration")
public final class WebDemoTranslatorFacade {
    public static final String JS_LIB_ROOT = new File(ApplicationSettings.WEBAPP_ROOT_DIRECTORY + File.separator + "js").getAbsolutePath();

    private static final List<String> LIBRARY_FILES = Arrays.asList("@stdlib", JS_LIB_ROOT);

    @SuppressWarnings("FieldCanBeLocal")
    private static String EXCEPTION = "exception=";

    private WebDemoTranslatorFacade() {
    }

    @SuppressWarnings("UnusedDeclaration")
    @Nullable
    public static BindingContext analyzeProgramCode(@NotNull JetFile file, SessionInfo sessionInfo) {
        try {
            return TopDownAnalyzerFacadeForJS.analyzeFiles(Arrays.asList(file), new LibrarySourcesConfig(
                    Initializer.INITIALIZER.getEnvironment().getProject(),
                    "moduleId",
                    LIBRARY_FILES,
                    EcmaVersion.defaultVersion(),
                    false,
                    true)).getBindingContext();
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.CONVERT_TO_JS.name(), sessionInfo.getOriginUrl(), file.getText());
            return null;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @NotNull
    public static String translateStringWithCallToMain(@NotNull String programText, @NotNull String argumentsString, SessionInfo sessionInfo) {
        try {
            JSONArray result = new JSONArray();
            TranslationResult translationResult = doTranslate(programText, argumentsString, sessionInfo);

            if (!translationResult.isSuccess) {
                return translationResult.getOutput();
            }

            Map<String, String> map = new HashMap<String, String>();
            map.put("text", translationResult.getOutput());
            result.put(map);

            return result.toString();
        } catch (MainFunctionNotFoundException te) {
            return ResponseUtils.getErrorInJson(te.getMessage());
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.CONVERT_TO_JS.name(), sessionInfo.getOriginUrl(), programText);
            KotlinCoreException ex = new KotlinCoreException(e);
            return ResponseUtils.getErrorWithStackTraceInJson(ApplicationSettings.KOTLIN_ERROR_MESSAGE, ex.getStackTraceString());
        }
        finally {
            Initializer.reinitializeJavaEnvironment();
        }
    }

    @NotNull
    private static TranslationResult doTranslate(@NotNull String programText,
                                                 @NotNull String argumentsString,
                                                 @NotNull SessionInfo sessionInfo
    ) throws TranslationException {
        LibrarySourcesConfig config = new LibrarySourcesConfig(
                Initializer.INITIALIZER.getEnvironment().getProject(),
                "moduleId",
                LIBRARY_FILES,
                EcmaVersion.defaultVersion(),
                false,
                true);
        K2JSTranslator translator = new K2JSTranslator(config);
        JetFile file = JetPsiFactoryUtil.createFile(Initializer.INITIALIZER.getEnvironment().getProject(), programText);
        org.jetbrains.kotlin.js.facade.TranslationResult result = translator.translate(Arrays.asList(file), MainCallParameters.mainWithArguments(Arrays.asList(ResponseUtils.splitArguments(argumentsString))));
        if (result instanceof org.jetbrains.kotlin.js.facade.TranslationResult.Success) {
            org.jetbrains.kotlin.js.facade.TranslationResult.Success success = ((org.jetbrains.kotlin.js.facade.TranslationResult.Success) result);
            return new TranslationResult(K2JSTranslator.FLUSH_SYSTEM_OUT + success.getCode() + "\n" + K2JSTranslator.GET_SYSTEM_OUT, true);
        }
        else {
            ArrayList<ErrorDescriptor> errorDescriptors = new ArrayList<ErrorDescriptor>();
            ErrorAnalyzer errorAnalyzer = new ErrorAnalyzer(file, sessionInfo);
            errorAnalyzer.processDiagnostics(result.getDiagnostics(), errorDescriptors);

            return new TranslationResult(JsonResponseForHighlighting.errorDescriptorsToString(errorDescriptors), false);
        }
    }


    private static class TranslationResult {
        private final boolean isSuccess;
        private final String output;

        public TranslationResult(String output, boolean isSuccess) {
            this.output = output;
            this.isSuccess = isSuccess;
        }

        public String getOutput() {
            return output;
        }
    }

}
