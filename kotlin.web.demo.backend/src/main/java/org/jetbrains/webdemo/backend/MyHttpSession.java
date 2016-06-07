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

package org.jetbrains.webdemo.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.Project;
import org.jetbrains.webdemo.ProjectFile;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.backend.executor.ExecutorUtils;
import org.jetbrains.webdemo.backend.executor.result.ExecutionResult;
import org.jetbrains.webdemo.kotlin.KotlinWrapper;
import org.jetbrains.webdemo.kotlin.KotlinWrappersManager;
import org.jetbrains.webdemo.kotlin.datastructures.CompilationResult;
import org.jetbrains.webdemo.kotlin.datastructures.CompletionVariant;
import org.jetbrains.webdemo.kotlin.datastructures.ErrorDescriptor;
import org.jetbrains.webdemo.kotlin.datastructures.TranslationResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyHttpSession {
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Project currentProject;
    private ObjectMapper objectMapper = new ObjectMapper();

    public void handle(final HttpServletRequest request, final HttpServletResponse response) {
        try {
            this.request = request;
            this.response = response;
            switch (request.getParameter("type")) {
                case ("run"):
                    sendExecutorResult();
                    break;
                case ("highlight"):
                    sendHighlightingResult();
                    break;
                case ("convertToKotlin"):
                    sendConversionResult();
                    break;
                case ("complete"):
                    sendCompletionResult();
                    break;
                case ("getKotlinVersions"):
                    sendKotlinVersions();
                    break;
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, "");
            writeResponse(ResponseUtils.getErrorInJson("Internal server error"), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendKotlinVersions() {
        try {
            writeResponse(objectMapper.writeValueAsString(KotlinWrappersManager.INSTANCE.getWrappersConfig()),
                    HttpServletResponse.SC_OK);
        } catch (IOException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, "Can't send kotlin versions");
            writeResponse("Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendConversionResult() {
        try {
            KotlinWrapper wrapper = KotlinWrappersManager.INSTANCE.getKotlinWrapper("1.0.1-2");
            String javaCode = request.getParameter("text");
            String kotlinCode = wrapper.translateJavaToKotlin(javaCode);
            writeResponse(kotlinCode, HttpServletResponse.SC_OK);
        } catch (Exception e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, "Error during java to kotlin translation");
            writeResponse("Internal server error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendExecutorResult() {
        try {
            currentProject = objectMapper.readValue(request.getParameter("project"), Project.class);
            KotlinWrapper wrapper = KotlinWrappersManager.INSTANCE.getKotlinWrapper("1.0.2");

            Map<String, String> files = getFilesContentFromProject(currentProject);
            boolean isJs = currentProject.confType.equals("js") || currentProject.confType.equals("canvas");
            Map<String, List<ErrorDescriptor>> errorDescriptors = wrapper.getErrors(files, isJs);

            if (!currentProject.confType.equals("js") && !currentProject.confType.equals("canvas")) {
                ExecutionResult executionResult = null;
                if (isOnlyWarnings(errorDescriptors)) {
                    CompilationResult compilationResult = wrapper.compileCorrectFiles(getFilesContentFromProject(currentProject));
                    executionResult = ExecutorUtils.executeCompiledFiles(
                            compilationResult.getFiles(),
                            compilationResult.getMainClass(),
                            wrapper.getKotlinRuntimeLibraries(),
                            wrapper.getKotlinCompilerJar(),
                            currentProject.args,
                            currentProject.confType.equals("junit")
                    );
                    executionResult.addWarningsFromCompiler(errorDescriptors);
                    //TODO add method positions for junit tests
                } else {
                    executionResult = new ExecutionResult(errorDescriptors);
                }

                writeResponse(objectMapper.writeValueAsString(executionResult), HttpServletResponse.SC_OK);
            } else {
                TranslationResult translationResult;
                if (isOnlyWarnings(errorDescriptors)) {
                    translationResult = wrapper.compileKotlinToJS(files, ResponseUtils.splitArguments(currentProject.args));
                    translationResult.addWarningsFromAnalyzer(errorDescriptors);
                } else {
                    translationResult = new TranslationResult(errorDescriptors);
                }
                writeResponse(objectMapper.writeValueAsString(translationResult), HttpServletResponse.SC_OK);
            }
        } catch (IOException e) {
            writeResponse("Can't parse project", HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            writeResponse(e.getMessage(), HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private boolean isOnlyWarnings(Map<String, List<ErrorDescriptor>> map) {
        for (String key : map.keySet()) {
            for (ErrorDescriptor errorDescriptor : map.get(key)) {
                if (errorDescriptor.getSeverity() == org.jetbrains.webdemo.kotlin.datastructures.Severity.ERROR) {
                    return false;
                }
            }
        }
        return true;
    }

    public void sendCompletionResult() {
        try {
            String fileName = request.getParameter("filename");
            int line = Integer.parseInt(request.getParameter("line"));
            int ch = Integer.parseInt(request.getParameter("ch"));
            currentProject = objectMapper.readValue(request.getParameter("project"), Project.class);
            KotlinWrapper wrapper = KotlinWrappersManager.INSTANCE.getKotlinWrapper("1.0.1-2");

            Map<String, String> files = getFilesContentFromProject(currentProject);
            boolean isJs = currentProject.confType.equals("js") || currentProject.confType.equals("canvas");
            List<CompletionVariant> completionVariants = wrapper.getCompletionVariants(files, fileName, line, ch, isJs);
            writeResponse(objectMapper.writeValueAsString(completionVariants), HttpServletResponse.SC_OK);
        } catch (IOException e) {
            writeResponse("Can't parse project", HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    public void sendHighlightingResult() {
        try {
            currentProject = objectMapper.readValue(request.getParameter("project"), Project.class);
            KotlinWrapper wrapper = KotlinWrappersManager.INSTANCE.getKotlinWrapper("1.0.1-2");
            Map<String, String> files = getFilesContentFromProject(currentProject);
            boolean isJs = currentProject.confType.equals("js") || currentProject.confType.equals("canvas");
            Map<String, List<ErrorDescriptor>> analysisResult = wrapper.getErrors(files, isJs);
            String response = objectMapper.writeValueAsString(analysisResult);
            response = response.replaceAll("\\n", "");
            writeResponse(response, HttpServletResponse.SC_OK);
        } catch (IOException e) {
            writeResponse("Can't parse project", HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private Map<String, String> getFilesContentFromProject(Project project) {
        Map<String, String> result = new HashMap<>();
        for (ProjectFile file : project.files) {
            result.put(file.getName(), file.getText());
        }
        return result;
    }

    //Send Response
    private void writeResponse(String responseBody, int statusCode) {
        try {
            response.setCharacterEncoding("UTF-8");
            response.setStatus(statusCode);
            if (!responseBody.equals("")) {
                try (PrintWriter writer = response.getWriter()) {
                    writer.write(responseBody);
                }
            }
        } catch (IOException e) {
            //This is an exception we can't send data to client
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, "");
        }
    }


}
