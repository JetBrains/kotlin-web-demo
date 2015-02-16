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

package org.jetbrains.webdemo.backend.responseHelpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.backend.BackendSettings;
import org.jetbrains.webdemo.backend.BackendSessionInfo;
import org.jetbrains.webdemo.backend.errorsDescriptors.ErrorAnalyzer;
import org.jetbrains.webdemo.backend.errorsDescriptors.ErrorDescriptor;
import org.jetbrains.webdemo.backend.exceptions.KotlinCoreException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonResponseForHighlighting {

    private final List<PsiFile> currentPsiFiles;
    private final Project currentProject;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final BackendSessionInfo sessionInfo;

    public JsonResponseForHighlighting(List<PsiFile> currentPsiFile, BackendSessionInfo info, Project currentProject) {
        this.currentPsiFiles = currentPsiFile;
        this.sessionInfo = info;
        this.currentProject = currentProject;
    }

    @NotNull
    public String getResult() {
        ErrorAnalyzer analyzer = new ErrorAnalyzer(currentPsiFiles, sessionInfo, currentProject);
        Map<String, List<ErrorDescriptor>> errorDescriptors;
        try {
            errorDescriptors = analyzer.getAllErrors();
        } catch (KotlinCoreException e) {
            return ResponseUtils.getErrorWithStackTraceInJson(BackendSettings.KOTLIN_ERROR_MESSAGE
                     , e.getStackTraceString());
        }
        try {
            return objectMapper.writeValueAsString(errorDescriptors);
        } catch (IOException e) {
            return "";
            //unreachable
        }
    }

}
