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
import org.apache.commons.lang.math.RandomUtils;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ErrorWriterOnServer;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.errorsDescriptors.ErrorAnalyzer;
import org.jetbrains.webdemo.errorsDescriptors.ErrorDescriptor;
import org.jetbrains.webdemo.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.server.ServerSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.jet.codegen.ClassBuilderFactories;
import org.jetbrains.jet.codegen.ClassFileFactory;
import org.jetbrains.jet.codegen.GenerationState;
import org.jetbrains.jet.lang.diagnostics.Severity;
import org.jetbrains.jet.lang.psi.JetFile;
import org.json.JSONArray;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Natalia.Ukhorskaya
 * Date: 10/20/11
 * Time: 1:41 PM
 */

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
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), currentPsiFile.getText());
            return ResponseUtils.getErrorWithStackTraceInJson(ServerSettings.KOTLIN_ERROR_MESSAGE, e.getStackTraceString());
        }

        if (errors.isEmpty() || isOnlyWarnings(errors)) {
            Project currentProject = currentPsiFile.getProject();
//            BindingContext bindingContext = AnalyzerFacade.analyzeOneFileWithJavaIntegration(
//                                (JetFile) currentPsiFile, JetControlFlowDataTraceFactory.EMPTY);

            sessionInfo.getTimeManager().saveCurrentTime();
            GenerationState generationState;
            try {
                generationState = new GenerationState(currentProject, ClassBuilderFactories.binaries(false));
                generationState.compile((JetFile) currentPsiFile);
//                generationState.compileCorrectFiles(bindingContext, Collections.singletonList((JetFile) currentPsiFile));
            } catch (Throwable e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), currentPsiFile.getText());
//                ErrorWriterOnServer.LOG_FOR_EXCEPTIONS.error(ErrorWriter.getExceptionForLog(sessionInfo.getType(), e, currentPsiFile.getText()));
                return ResponseUtils.getErrorWithStackTraceInJson(ServerSettings.KOTLIN_ERROR_MESSAGE, new KotlinCoreException(e).getStackTraceString());
            }
            ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(), sessionInfo.getId(),
                    "COMPILE correctNamespaces " + sessionInfo.getTimeManager().getMillisecondsFromSavedTime()));

            StringBuilder stringBuilder = new StringBuilder("Generated classfiles: ");
            stringBuilder.append(ResponseUtils.addNewLine());
            final ClassFileFactory factory = generationState.getFactory();

            sessionInfo.getTimeManager().saveCurrentTime();
            List<String> files = factory.files();
            File outputDir = new File(ServerSettings.OUTPUT_DIRECTORY + File.separator + "tmp" + RandomUtils.nextInt());
            boolean isOutputExists = true;
            if (!outputDir.exists()) {
                isOutputExists = outputDir.mkdirs();
            }
            for (String file : files) {
//                File outputDir = new File(ServerSettings.OUTPUT_DIRECTORY);
//                ServerSettings.OUTPUT_DIRECTORY = outputDir.getAbsolutePath();
                if (isOutputExists) {
                    File target = new File(outputDir, file);
                    try {
                        FileUtil.writeToFile(target, factory.asBytes(file));
                        stringBuilder.append(file).append(ResponseUtils.addNewLine());
                    } catch (IOException e) {
                        ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                                sessionInfo.getType(), currentPsiFile.getText());
                        return ResponseUtils.getErrorInJson("Cannot get a completion.");
                    }
                } else {
                    ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(new UnsupportedOperationException("Cannot create output directory for files"),
                            SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name(), currentPsiFile.getText());
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

            JavaRunner runner = new JavaRunner(files, arguments, jsonArray, currentPsiFile.getText(), sessionInfo);

            return runner.getResult(outputDir.getAbsolutePath());

        } else {
            return generateResponseWithErrors(errors);
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

    private String generateResponseWithErrors(List<ErrorDescriptor> errors) {
        JSONArray jsonArray = new JSONArray();

        for (ErrorDescriptor error : errors) {
            StringBuilder message = new StringBuilder();
            message.append("(").append(error.getInterval().startPoint.line + 1).append(", ").append(error.getInterval().startPoint.charNumber + 1).append(")");
//            message.append(error.getInterval());
            message.append(" - ");
            message.append(error.getMessage());
            Map<String, String> map = new HashMap<String, String>();
            map.put("type", error.getSeverity().name());
            map.put("message", message.toString());
            jsonArray.put(map);
        }

        return jsonArray.toString();
    }

}
