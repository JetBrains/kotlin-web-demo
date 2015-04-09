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

package org.jetbrains.webdemo.backend.responseHelpers;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.jetbrains.webdemo.JsonUtils;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.backend.BackendSessionInfo;
import org.jetbrains.webdemo.backend.BackendSettings;
import org.jetbrains.webdemo.backend.BackendUtils;
import org.jetbrains.webdemo.backend.ResolveUtils;
import org.jetbrains.webdemo.backend.errorsDescriptors.ErrorAnalyzer;
import org.jetbrains.webdemo.backend.errorsDescriptors.ErrorDescriptor;
import org.jetbrains.webdemo.backend.exceptions.KotlinCoreException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeoutException;

public class CompileAndRunExecutor {

    private final List<PsiFile> currentPsiFiles;

    private final BackendSessionInfo sessionInfo;
    private final String args;
    private final com.intellij.openapi.project.Project currentProject;

    public CompileAndRunExecutor(List<PsiFile> currentPsiFiles, com.intellij.openapi.project.Project currentProject, BackendSessionInfo info, String args) {
        this.currentPsiFiles = currentPsiFiles;
        this.currentProject = currentProject;
        this.sessionInfo = info;
        this.args = args;
    }

    public String getResult() throws Exception {
        ErrorAnalyzer analyzer = new ErrorAnalyzer(currentPsiFiles, sessionInfo, currentProject);
        Map<String,List<ErrorDescriptor>> errors;
        try {
            errors = analyzer.getAllErrors();
        } catch (KotlinCoreException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), BackendUtils.getPsiFilesContent(currentPsiFiles));
            return ResponseUtils.getErrorWithStackTraceInJson(BackendSettings.KOTLIN_ERROR_MESSAGE, e.getStackTraceString());
        }

        ArrayNode jsonArray = new ArrayNode(JsonNodeFactory.instance);
        ObjectNode errorsJson = jsonArray.addObject();
        errorsJson.put("type", "errors");
        errorsJson.put("errors", JsonUtils.getObjectMapper().valueToTree(errors));

        if (errors.isEmpty() || isOnlyWarnings(errors)) {
//            Project currentProject = currentPsiFile.getProject();
            sessionInfo.getTimeManager().saveCurrentTime();
            GenerationState generationState;
            try {

                generationState = ResolveUtils.getGenerationState(convertList(currentPsiFiles), currentProject);
                KotlinCodegenFacade.compileCorrectFiles(generationState, new CompilationErrorHandler() {
                    @Override
                    public void reportException(Throwable throwable, String s) {
                        ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(throwable, sessionInfo.getType(), sessionInfo.getOriginUrl(), s + " ");
                    }
                });
            } catch (Throwable e) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), BackendUtils.getPsiFilesContent(currentPsiFiles));
                return ResponseUtils.getErrorWithStackTraceInJson(BackendSettings.KOTLIN_ERROR_MESSAGE, new KotlinCoreException(e).getStackTraceString());
            }
//            ErrorWriter.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(), sessionInfo.getId(),
//                    "COMPILE correctNamespaces " + sessionInfo.getTimeManager().getMillisecondsFromSavedTime()));

            StringBuilder stringBuilder = new StringBuilder("Generated classfiles: ");
            stringBuilder.append(ResponseUtils.addNewLine());
            final ClassFileFactory factory = generationState.getFactory();

            sessionInfo.getTimeManager().saveCurrentTime();
            List<OutputFile> files = factory.asList();
            File outputDir = new File(BackendSettings.OUTPUT_DIRECTORY + File.separator + "tmp" + new Random().nextInt());
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
                                sessionInfo.getType(), sessionInfo.getOriginUrl(), BackendUtils.getPsiFilesContent(currentPsiFiles));
                        return ResponseUtils.getErrorInJson("Cannot get a result.");
                    }
                } else {
                    ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(new UnsupportedOperationException("Cannot create output directory for files"),
                            sessionInfo.getType(), sessionInfo.getOriginUrl(), BackendUtils.getPsiFilesContent(currentPsiFiles));
                    return ResponseUtils.getErrorInJson("Error on server: cannot run your program.");
                }

            }
//            ErrorWriter.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(), sessionInfo.getId(),
//                    "Write files on disk " + sessionInfo.getTimeManager().getMillisecondsFromSavedTime()));

            ObjectNode jsonObject = jsonArray.addObject();
            jsonObject.put("type", "info");
            jsonObject.put("text", stringBuilder.toString());


            JavaRunner runner = new JavaRunner(generationState.getBindingContext(), files, args, jsonArray, (JetFile) currentPsiFiles.get(0), sessionInfo);

            runner.getResult(outputDir.getAbsolutePath());
        }

        return jsonArray.toString();
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
