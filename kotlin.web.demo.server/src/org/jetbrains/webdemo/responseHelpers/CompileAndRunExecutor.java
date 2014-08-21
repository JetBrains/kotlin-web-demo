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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import org.jetbrains.jet.OutputFile;
import org.jetbrains.jet.codegen.ClassFileFactory;
import org.jetbrains.jet.codegen.KotlinCodegenFacade;
import org.jetbrains.jet.codegen.state.GenerationState;
import org.jetbrains.jet.lang.diagnostics.Severity;
import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ErrorWriterOnServer;
import org.jetbrains.webdemo.ResolveUtils;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.errorsDescriptors.ErrorAnalyzer;
import org.jetbrains.webdemo.errorsDescriptors.ErrorDescriptor;
import org.jetbrains.webdemo.examplesLoader.ExampleObject;
import org.jetbrains.webdemo.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class CompileAndRunExecutor {

    private final List<PsiFile> currentPsiFiles;

    private final SessionInfo sessionInfo;
    private final ExampleObject example;
    private final Project currentProject;

    public CompileAndRunExecutor(List<PsiFile> currentPsiFiles, Project currentProject, SessionInfo info, ExampleObject example) {
        this.currentPsiFiles = currentPsiFiles;
        this.currentProject = currentProject;
        this.sessionInfo = info;
        this.example = example;
    }

    public String getResult() {
        ErrorAnalyzer analyzer = new ErrorAnalyzer(currentPsiFiles, sessionInfo, currentProject);
        Map<String,List<ErrorDescriptor>> errors;
        try {
            errors = analyzer.getAllErrors();
        } catch (KotlinCoreException e) {
//            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
            return ResponseUtils.getErrorWithStackTraceInJson(ApplicationSettings.KOTLIN_ERROR_MESSAGE, e.getStackTraceString());
        }

        if (errors.isEmpty() || isOnlyWarnings(errors)) {
//            Project currentProject = currentPsiFile.getProject();
            sessionInfo.getTimeManager().saveCurrentTime();
            GenerationState generationState;
            try {

                generationState = ResolveUtils.getGenerationState(convertList(currentPsiFiles), currentProject);
                KotlinCodegenFacade.compileCorrectFiles(generationState, (throwable, s) -> ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(throwable, sessionInfo.getType(), sessionInfo.getOriginUrl(), s + " " /*+ currentPsiFile.getText()*/));
            } catch (Throwable e) {
//                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
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
//                        ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
//                                sessionInfo.getType(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
                        return ResponseUtils.getErrorInJson("Cannot get a result.");
                    }
                } else {
//                    ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(new UnsupportedOperationException("Cannot create output directory for files"),
//                            SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
                    return ResponseUtils.getErrorInJson("Error on server: cannot run your program.");
                }

            }
            ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(), sessionInfo.getId(),
                    "Write files on disk " + sessionInfo.getTimeManager().getMillisecondsFromSavedTime()));

            ArrayNode jsonArray = new ArrayNode(JsonNodeFactory.instance);
            ObjectNode jsonObject = jsonArray.addObject();
            jsonObject.put("type", "info");
            jsonObject.put("text", stringBuilder.toString());


            JavaRunner runner = new JavaRunner(generationState.getBindingContext(), files, example.args, jsonArray, (JetFile) currentPsiFiles.get(0), sessionInfo, example);

            return runner.getResult(outputDir.getAbsolutePath());

        } else {
            return ResponseUtils.getErrorInJson("There are errors in your code.");
        }
    }

    private List<JetFile> convertList(List<PsiFile> list){
        List<JetFile> ans = new ArrayList<>();
        for(PsiFile psiFile : list){
            ans.add((JetFile)psiFile);
        }
        return ans;
    }

    private boolean isOnlyWarnings(Map<String, List<ErrorDescriptor>> map) {
        for(String key: map.keySet()) {
            for (ErrorDescriptor errorDescriptor : map.get(key)) {
                if (errorDescriptor.getSeverity() == Severity.ERROR) {
                    return false;
                }
            }
        }
        return true;
    }
}
