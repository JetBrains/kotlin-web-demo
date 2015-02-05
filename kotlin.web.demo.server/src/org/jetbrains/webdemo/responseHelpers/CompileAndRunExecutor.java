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

package org.jetbrains.webdemo.responseHelpers;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import org.jetbrains.kotlin.backend.common.output.OutputFile;
import org.jetbrains.kotlin.codegen.ClassFileFactory;
import org.jetbrains.kotlin.codegen.CompilationErrorHandler;
import org.jetbrains.kotlin.codegen.KotlinCodegenFacade;
import org.jetbrains.kotlin.codegen.state.GenerationState;
import org.jetbrains.kotlin.diagnostics.Severity;
import org.jetbrains.kotlin.psi.JetFile;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ErrorWriterOnServer;
import org.jetbrains.webdemo.ResolveUtils;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.errorsDescriptors.ErrorAnalyzer;
import org.jetbrains.webdemo.errorsDescriptors.ErrorDescriptor;
import org.jetbrains.webdemo.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CompileAndRunExecutor {

    private final PsiFile currentPsiFile;
    private final String arguments;

    private final SessionInfo sessionInfo;

    public CompileAndRunExecutor(PsiFile currentPsiFile, String arguments, SessionInfo info) {
        this.currentPsiFile = currentPsiFile;
        this.arguments = arguments;
        this.sessionInfo = info;
    }

    public String getResult() {
        ErrorAnalyzer analyzer = new ErrorAnalyzer(currentPsiFile, sessionInfo);
        List<ErrorDescriptor> errors;
        try {
            errors = analyzer.getAllErrors();
        } catch (KotlinCoreException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
            return ResponseUtils.getErrorWithStackTraceInJson(ApplicationSettings.KOTLIN_ERROR_MESSAGE, e.getStackTraceString());
        }

        if (errors.isEmpty() || isOnlyWarnings(errors)) {
            Project currentProject = currentPsiFile.getProject();
            sessionInfo.getTimeManager().saveCurrentTime();
            GenerationState generationState;
            try {
                generationState = ResolveUtils.getGenerationState((JetFile) currentPsiFile);
                KotlinCodegenFacade.compileCorrectFiles(generationState, new CompilationErrorHandler() {
                    @Override
                    public void reportException(Throwable throwable, String s) {
                        ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(throwable, sessionInfo.getType(), sessionInfo.getOriginUrl(), s + " " + currentPsiFile.getText());
                    }
                });
            } catch (Throwable e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
                return ResponseUtils.getErrorWithStackTraceInJson(ApplicationSettings.KOTLIN_ERROR_MESSAGE, new KotlinCoreException(e).getStackTraceString());
            }
            ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(), sessionInfo.getId(),
                    "COMPILE correctNamespaces " + sessionInfo.getTimeManager().getMillisecondsFromSavedTime()));

            StringBuilder stringBuilder = new StringBuilder("Generated classfiles: ");
            stringBuilder.append(ResponseUtils.addNewLine());
            final ClassFileFactory factory = generationState.getFactory();

            sessionInfo.getTimeManager().saveCurrentTime();
            List<OutputFile> files = factory.asList();
            File outputDir = new File(ApplicationSettings.OUTPUT_DIRECTORY + File.separator + "tmp" + new Random().nextInt());
            boolean isOutputExists = true;
            if (!outputDir.exists()) {
                isOutputExists = outputDir.mkdirs();
            }
            for (OutputFile file : files) {
                if (isOutputExists) {
                    File target = new File(outputDir, file.getRelativePath());
                    try {
                        FileUtil.writeToFile(target, file.asByteArray());
                        stringBuilder.append(file.getRelativePath()).append(ResponseUtils.addNewLine());
                    } catch (IOException e) {
                        ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                                sessionInfo.getType(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
                        return ResponseUtils.getErrorInJson("Cannot get a result.");
                    }
                } else {
                    ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(new UnsupportedOperationException("Cannot create output directory for files"),
                            SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
                    return ResponseUtils.getErrorInJson("Error on server: cannot run your program.");
                }

            }
            ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(), sessionInfo.getId(),
                    "Write files on disk " + sessionInfo.getTimeManager().getMillisecondsFromSavedTime()));

            JSONArray jsonArray = new JSONArray();
            Map<String, String> map = new HashMap<String, String>();
            map.put("type", "info");
            map.put("text", stringBuilder.toString());
            jsonArray.put(map);

            JavaRunner runner = new JavaRunner(generationState.getBindingContext(), files, arguments, jsonArray, (JetFile) currentPsiFile, sessionInfo);

            return runner.getResult(outputDir.getAbsolutePath());

        } else {
            return ResponseUtils.getErrorInJson("There are errors in your code.");
        }
    }

    private boolean isOnlyWarnings(List<ErrorDescriptor> list) {
        for (ErrorDescriptor errorDescriptor : list) {
            if (errorDescriptor.getSeverity() == Severity.ERROR) {
                return false;
            }
        }
        return true;
    }
}
