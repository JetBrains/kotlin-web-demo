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
import com.intellij.psi.PsiFile;
import org.jetbrains.webdemo.*;
import org.jetbrains.webdemo.backend.executor.ProgramOutput;
import org.jetbrains.webdemo.backend.executor.ExecutorUtils;
import org.jetbrains.webdemo.backend.executor.result.ExecutionResult;
import org.jetbrains.webdemo.kotlin.KotlinWrapper;
import org.jetbrains.webdemo.kotlin.KotlinWrappersManager;
import org.jetbrains.webdemo.kotlin.datastructures.CompilationResult;
import org.jetbrains.webdemo.kotlin.datastructures.CompletionVariant;
import org.jetbrains.webdemo.kotlin.datastructures.ErrorDescriptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyHttpSession {
    private final BackendSessionInfo sessionInfo;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Project currentProject;
    private ObjectMapper objectMapper = new ObjectMapper();

    public MyHttpSession(BackendSessionInfo info) {
        this.sessionInfo = info;
    }

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
                    sessionInfo.setType(BackendSessionInfo.TypeOfRequest.CONVERT_TO_KOTLIN);
                    sendConversationResult();
                    break;
                case ("complete"):
                    sessionInfo.setType(BackendSessionInfo.TypeOfRequest.COMPLETE);
                    sendCompletionResult();
                    break;
            }
        } catch (Throwable e) {
            e.printStackTrace();
            if (sessionInfo != null && sessionInfo.getType() != null && currentProject != null) {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), JsonUtils.toJson(currentProject));
            } else {
                ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, "UNKNOWN", "unknown", "null");
            }
            writeResponse(ResponseUtils.getErrorInJson("Internal server error"), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private void sendConversationResult() {
        try {
            KotlinWrapper wrapper = KotlinWrappersManager.getKotlinWrapper("1.0.1-2");
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
            KotlinWrapper wrapper = KotlinWrappersManager.getKotlinWrapper("1.0.1-2");

            Map<String, List<ErrorDescriptor>> errorDescriptors = wrapper.getErrors(currentProject);

            if (isOnlyWarnings(errorDescriptors)) {
                if (!currentProject.confType.equals("js") && !currentProject.confType.equals("canvas")) {
                    CompilationResult compilationResult = wrapper.compileCorrectFiles(currentProject);
                    ExecutionResult programOutput = ExecutorUtils.executeCompiledFiles(
                            compilationResult.getFiles(),
                            compilationResult.getMainClass(),
                            wrapper.getKotlinRuntimeLibraries(),
                            wrapper.getKotlinCompilerJar(),
                            currentProject.args,
                            currentProject.confType.equals("junit")
                    );
                    programOutput.addWarningsFromCompiler(errorDescriptors);
                    writeResponse(objectMapper.writeValueAsString(programOutput), HttpServletResponse.SC_OK);
                } else {
//                sessionInfo.setType(BackendSessionInfo.TypeOfRequest.CONVERT_TO_JS);
//                writeResponse(new JsConverter(sessionInfo).getResult(psiFiles, sessionInfo, currentProject.args), HttpServletResponse.SC_OK);
                }
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

    private List<PsiFile> createProjectPsiFiles(Project example) {
        List<PsiFile> result = new ArrayList<>();
        for (ProjectFile file : example.files) {
            result.add(JetPsiFactoryUtil.createFile(Initializer.getInstance().getEnvironment().getProject(), file.getName(), file.getText()));
        }
        return result;
    }

    public void sendCompletionResult() {
        try {
            String fileName = request.getParameter("filename");
            int line = Integer.parseInt(request.getParameter("line"));
            int ch = Integer.parseInt(request.getParameter("ch"));
            currentProject = objectMapper.readValue(request.getParameter("project"), Project.class);
            KotlinWrapper wrapper = KotlinWrappersManager.getKotlinWrapper("1.0.1-2");

            List<CompletionVariant> completionVariants = wrapper.getCompletionVariants(currentProject, fileName, line, ch);
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
            KotlinWrapper wrapper = KotlinWrappersManager.getKotlinWrapper("1.0.1-2");
            Map<String, List<ErrorDescriptor>> analysisResult = wrapper.getErrors(currentProject);
            String response = objectMapper.writeValueAsString(analysisResult);
            response = response.replaceAll("\\n", "");
            writeResponse(response, HttpServletResponse.SC_OK);
        } catch (IOException e) {
            writeResponse("Can't parse project", HttpServletResponse.SC_BAD_REQUEST);
        } catch (NullPointerException e) {
            writeResponse("Can't get parameters", HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    //Send Response
    private void writeResponse(String responseBody, int statusCode) {
        try {
            ResponseUtils.writeResponse(request, response, responseBody, statusCode);
        } catch (IOException e) {
            //This is an exception we can't send data to client
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), JsonUtils.toJson(currentProject));
        }
    }


}
