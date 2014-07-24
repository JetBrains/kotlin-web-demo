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
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.psi.PsiFile;
import org.jetbrains.jet.OutputFile;
import org.jetbrains.jet.codegen.CompilationErrorHandler;
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
import org.jetbrains.webdemo.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
        try {
            List<ErrorDescriptor> errors = getErrors();

            if (errors.isEmpty() || isOnlyWarnings(errors)) {
                GenerationState generationState = compileFiles();

                List<OutputFile> files =  generationState.getFactory().asList();
                File outputDir = new File(ApplicationSettings.OUTPUT_DIRECTORY + File.separator + "tmp" + new Random().nextInt());
                String generatedClassfilesInfo = generateClassfiles(files, outputDir);

                ArrayNode jsonArray = new ArrayNode(JsonNodeFactory.instance);
                ObjectNode jsonObject = jsonArray.addObject();
                jsonObject.put("type", "info");
                jsonObject.put("text", generatedClassfilesInfo);


                JavaRunner runner = new JavaRunner(generationState.getBindingContext(), files, arguments, jsonArray, (JetFile) currentPsiFile, sessionInfo);

                return runner.getResult(outputDir.getAbsolutePath());

            } else {
                return ResponseUtils.getErrorInJson("There are errors in your code.");
            }
        } catch (Exception e){
            return e.getMessage();
        }
    }

    private String generateClassfiles(List<OutputFile> files, File outputDir) throws IOException {
        sessionInfo.getTimeManager().saveCurrentTime();
        StringBuilder stringBuilder = new StringBuilder("Generated classfiles: ");
        stringBuilder.append(ResponseUtils.addNewLine());

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
                    throw new IOException( ResponseUtils.getErrorInJson("Cannot get a result."), e);
                }
            } else {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(new UnsupportedOperationException("Cannot create output directory for files"),
                        SessionInfo.TypeOfRequest.DOWNLOAD_LOG.name(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
                throw new IOException( ResponseUtils.getErrorInJson("Error on server: cannot run your program.") );
            }

        }
        ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(), sessionInfo.getId(),
                "Write files on disk " + sessionInfo.getTimeManager().getMillisecondsFromSavedTime()));
        return stringBuilder.toString();
    }

    private GenerationState compileFiles() throws Exception {
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

            ErrorWriterOnServer.LOG_FOR_INFO.info(ErrorWriter.getInfoForLogWoIp(sessionInfo.getType(), sessionInfo.getId(),
                    "COMPILE correctNamespaces " + sessionInfo.getTimeManager().getMillisecondsFromSavedTime()));
            return generationState;
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
            throw new Exception(ResponseUtils.getErrorWithStackTraceInJson(ApplicationSettings.KOTLIN_ERROR_MESSAGE, new KotlinCoreException(e).getStackTraceString()), e);
        }
    }

    private List<ErrorDescriptor> getErrors() throws Exception {
        ErrorAnalyzer analyzer = new ErrorAnalyzer(currentPsiFile, sessionInfo);
        try {
            return analyzer.getAllErrors();
        } catch (KotlinCoreException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), currentPsiFile.getText());
            throw new Exception(ResponseUtils.getErrorWithStackTraceInJson(ApplicationSettings.KOTLIN_ERROR_MESSAGE, e.getStackTraceString()), e);
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
