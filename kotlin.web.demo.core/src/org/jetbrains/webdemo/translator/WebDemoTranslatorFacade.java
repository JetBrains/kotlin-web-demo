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
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.k2js.analyze.TopDownAnalyzerFacadeForJS;
import org.jetbrains.k2js.config.Config;
import org.jetbrains.k2js.config.EcmaVersion;
import org.jetbrains.k2js.config.LibrarySourcesConfig;
import org.jetbrains.k2js.facade.K2JSTranslator;
import org.jetbrains.k2js.facade.MainCallParameters;
import org.jetbrains.k2js.facade.exceptions.MainFunctionNotFoundException;
import org.jetbrains.k2js.facade.exceptions.TranslationException;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.Initializer;
import org.jetbrains.webdemo.JetPsiFactoryUtil;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.json.JSONArray;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    true));
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
            Map<String, String> map = new HashMap<String, String>();
            map.put("text", doTranslate(programText, argumentsString));
            result.put(map);

            Initializer.reinitializeJavaEnvironment();

            return result.toString();

        } catch (MainFunctionNotFoundException te) {
            Initializer.reinitializeJavaEnvironment();
            return ResponseUtils.getErrorInJson(te.getMessage()); 
        } catch (Throwable e) {
            Initializer.reinitializeJavaEnvironment();

            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.CONVERT_TO_JS.name(), sessionInfo.getOriginUrl(), programText);
            KotlinCoreException ex = new KotlinCoreException(e);
            return ResponseUtils.getErrorWithStackTraceInJson(ApplicationSettings.KOTLIN_ERROR_MESSAGE, ex.getStackTraceString());
        }
    }

    @NotNull
    private static String doTranslate(@NotNull String programText,
                                      @NotNull String argumentsString) throws TranslationException {
        K2JSTranslator translator = new K2JSTranslator(new LibrarySourcesConfig(
                Initializer.INITIALIZER.getEnvironment().getProject(),
                "moduleId",
                LIBRARY_FILES,
                EcmaVersion.defaultVersion(),
                false,
                true));
        JetFile file = JetPsiFactoryUtil.createFile(Initializer.INITIALIZER.getEnvironment().getProject(), programText);
        String programCode = translator.generateProgramCode(file, MainCallParameters.mainWithArguments(Arrays.asList(ResponseUtils.splitArguments(argumentsString)))) + "\n";
        return K2JSTranslator.FLUSH_SYSTEM_OUT + programCode + K2JSTranslator.GET_SYSTEM_OUT;
    }

}
