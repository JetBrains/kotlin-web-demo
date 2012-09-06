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
import org.jetbrains.k2js.analyze.AnalyzerFacadeForJS;
import org.jetbrains.k2js.config.Config;
import org.jetbrains.k2js.facade.K2JSTranslator;
import org.jetbrains.k2js.facade.MainCallParameters;
import org.jetbrains.k2js.facade.exceptions.MainFunctionNotFoundException;
import org.jetbrains.k2js.facade.exceptions.TranslationException;
import org.jetbrains.k2js.utils.JetFileUtils;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.Initializer;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.json.JSONArray;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pavel Talanov
 */
@SuppressWarnings("UnusedDeclaration")
public final class WebDemoTranslatorFacade {

    public static Config LOAD_JS_LIBRARY_CONFIG;

    public static final String FLUSH_SYSTEM_OUT = "Kotlin.System.flush();\n";
    public static final String GET_SYSTEM_OUT = "Kotlin.System.output();\n";

    @SuppressWarnings("FieldCanBeLocal")
    private static String EXCEPTION = "exception=";

    private WebDemoTranslatorFacade() {
    }

    @SuppressWarnings("UnusedDeclaration")
    @Nullable
    public static BindingContext analyzeProgramCode(@NotNull JetFile file) {
        try {
            BindingContext bindingContext = AnalyzerFacadeForJS.analyzeFiles(Arrays.asList(file), LOAD_JS_LIBRARY_CONFIG);
            Initializer.reinitializeJavaEnvironment();
            return bindingContext;
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.CONVERT_TO_JS.name(), file.getText());
            return null;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @NotNull
    public static String translateStringWithCallToMain(@NotNull String programText, @NotNull String argumentsString) {
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
                    SessionInfo.TypeOfRequest.CONVERT_TO_JS.name(), programText);
            KotlinCoreException ex = new KotlinCoreException(e);
            return ResponseUtils.getErrorWithStackTraceInJson(ApplicationSettings.KOTLIN_ERROR_MESSAGE, ex.getStackTraceString());
        }
    }

    @NotNull
    private static String doTranslate(@NotNull String programText,
                                      @NotNull String argumentsString) throws TranslationException {
        K2JSTranslator translator = new K2JSTranslator(LOAD_JS_LIBRARY_CONFIG);
        JetFile file = JetFileUtils.createPsiFile("test", programText, Initializer.INITIALIZER.getEnvironment().getProject());
        String programCode = translator.generateProgramCode(file, MainCallParameters.mainWithArguments(Arrays.asList(ResponseUtils.splitArguments(argumentsString)))) + "\n";
        return FLUSH_SYSTEM_OUT + programCode + GET_SYSTEM_OUT;
    }

}
