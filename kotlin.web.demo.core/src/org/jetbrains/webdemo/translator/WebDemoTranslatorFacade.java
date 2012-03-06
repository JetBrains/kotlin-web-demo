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
import org.jetbrains.k2js.facade.K2JSTranslator;
import org.jetbrains.k2js.utils.JetFileUtils;

import java.util.Arrays;

/**
 * @author Pavel Talanov
 */
@SuppressWarnings("UnusedDeclaration")
public final class WebDemoTranslatorFacade {

    @SuppressWarnings("FieldCanBeLocal")
    private static String EXCEPTION = "exception=";

    private WebDemoTranslatorFacade() {
    }

    @SuppressWarnings("UnusedDeclaration")
    @Nullable
    public static BindingContext analyzeProgramCode(@NotNull Project project, @NotNull JetFile file) {
        try {
            return AnalyzerFacadeForJS.analyzeFiles(Arrays.asList(file), new WebDemoConfig(project));
        } catch (Throwable e) {
            reportException(e);
            return null;
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    @NotNull
    public static String translateStringWithCallToMain(@NotNull Project project,
                                                       @NotNull String programText, @NotNull String argumentsString) {
        try {
            return doTranslate(project, programText, argumentsString);

        } catch (AssertionError e) {
            reportException(e);
            return EXCEPTION + "Translation error.";
        } catch (UnsupportedOperationException e) {
            reportException(e);
            return EXCEPTION + "Unsupported feature.";
        } catch (Throwable e) {
            reportException(e);
            return EXCEPTION + "Unexpected exception.";
        }
    }

    @NotNull
    private static String doTranslate(@NotNull Project project, @NotNull String programText,
                                      @NotNull String argumentsString) {
        K2JSTranslator translator = new org.jetbrains.k2js.facade.K2JSTranslator(new WebDemoConfig(project));
        JetFile file = JetFileUtils.createPsiFile("test", programText, project);

        String programCode = translator.generateProgramCode(file) + "\n";
        String flushOutput = "Kotlin.System.flush();\n";
        String callToMain = org.jetbrains.k2js.facade.K2JSTranslator.generateCallToMain(file, argumentsString);
        String programOutput = "Kotlin.System.output();\n";
        return programCode + flushOutput + callToMain + programOutput;
    }

    private static void reportException(@NotNull Throwable e) {
        e.printStackTrace();
    }
}
