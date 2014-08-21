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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.webdemo.Interval;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.errorsDescriptors.ErrorAnalyzer;
import org.jetbrains.webdemo.errorsDescriptors.ErrorDescriptor;
import org.jetbrains.webdemo.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JsonResponseForHighlighting {

    private final List<PsiFile> currentPsiFiles;
    private final Project currentProject;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    private final SessionInfo sessionInfo;

    public JsonResponseForHighlighting(List<PsiFile> currentPsiFile, SessionInfo info, Project currentProject) {
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
            return ResponseUtils.getErrorWithStackTraceInJson(ApplicationSettings.KOTLIN_ERROR_MESSAGE
                     , e.getStackTraceString());
        }
        ArrayNode resultArray = new ArrayNode(JsonNodeFactory.instance);

//        for (ErrorDescriptor errorDescriptor : errorDescriptors) {
//            resultArray.add(getMapForJsonResponse(errorDescriptor.getInterval(), errorDescriptor.getMessage(),
//                    errorDescriptor.getClassName(), errorDescriptor.getSeverity().name()));
//        }
        try {
            return objectMapper.writeValueAsString(errorDescriptors);
        } catch (IOException e) {
            return "";
            //unreachable
        }
    }

    @NotNull
    private ObjectNode getMapForJsonResponse(Interval interval, String titleName, String className, String severity) {
        ObjectNode jsonObject = new ObjectNode(JsonNodeFactory.instance);

        jsonObject.put("x", "{line: " + interval.start.line + ", ch: " + interval.start.ch + "}");
        jsonObject.put("y", "{line: " + interval.end.line + ", ch: " + interval.end.ch + "}");
        jsonObject.put("titleName", escape(titleName));
        jsonObject.put("className", className);
        jsonObject.put("severity", severity);
        return jsonObject;
    }


    private String escape(String str) {
        if ((str != null) && (str.contains("\""))) {
            str = str.replaceAll("\\\"", "'");
        }
        return str;
    }

}
