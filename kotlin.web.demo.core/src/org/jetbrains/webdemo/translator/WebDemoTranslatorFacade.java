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

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.k2js.analyze.AnalyzerFacadeForJS;
import org.jetbrains.k2js.config.Config;
import org.jetbrains.k2js.facade.K2JSTranslator;
import org.jetbrains.k2js.utils.JetFileUtils;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.Initializer;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.session.SessionInfo;

import java.util.Arrays;

/**
 * @author Pavel Talanov
 */
@SuppressWarnings("UnusedDeclaration")
public final class WebDemoTranslatorFacade {

    public static Config LOAD_JS_LIBRARY_CONFIG;

    @SuppressWarnings("FieldCanBeLocal")
    private static String EXCEPTION = "exception=";

    private WebDemoTranslatorFacade() {
    }

    @SuppressWarnings("UnusedDeclaration")
    @Nullable
    public static BindingContext analyzeProgramCode(@NotNull JetFile file) {
        try {
//            LOAD_JS_LIBRARY_CONFIG.setProject(Initializer.INITIALIZER.getEnvironment().getProject());
            return AnalyzerFacadeForJS.analyzeFiles(Arrays.asList(file), LOAD_JS_LIBRARY_CONFIG);
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
            return doTranslate(programText, argumentsString);

        } catch (AssertionError e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.CONVERT_TO_JS.name(), programText);
            return ResponseUtils.getErrorInJson("Translation error.");
        } catch (UnsupportedOperationException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.CONVERT_TO_JS.name(), programText);
            return ResponseUtils.getErrorInJson("Unsupported feature.");
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.CONVERT_TO_JS.name(), programText);
            return ResponseUtils.getErrorInJson("Unexpected exception.");
        }
    }

    @NotNull
    private static String doTranslate(@NotNull String programText,
                                      @NotNull String argumentsString) {
        try {
            K2JSTranslator translator = new K2JSTranslator(LOAD_JS_LIBRARY_CONFIG);
            JetFile file = JetFileUtils.createPsiFile("test", programText, Initializer.INITIALIZER.getEnvironment().getProject());

            String programCode = translator.generateProgramCode(file) + "\n";
            String flushOutput = "Kotlin.System.flush();\n";
            String callToMain = K2JSTranslator.generateCallToMain(file, argumentsString);
            String programOutput = "Kotlin.System.output();\n";
            return programCode + flushOutput + callToMain + programOutput;
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    SessionInfo.TypeOfRequest.CONVERT_TO_JS.name(), programText);
            return "";
        }
    }

}
