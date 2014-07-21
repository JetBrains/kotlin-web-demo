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
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.webdemo.Interval;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.errorsDescriptors.ErrorAnalyzer;
import org.jetbrains.webdemo.errorsDescriptors.ErrorDescriptor;
import org.jetbrains.webdemo.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;

import java.util.List;

public class JsonResponseForHighlighting {

    private final PsiFile currentPsiFile;
    
    private final SessionInfo sessionInfo;

    public JsonResponseForHighlighting(PsiFile currentPsiFile, SessionInfo info) {
        this.currentPsiFile = currentPsiFile;
        this.sessionInfo = info;
    }

    @NotNull
    public String getResult() {
        ErrorAnalyzer analyzer = new ErrorAnalyzer(currentPsiFile, sessionInfo);
        List<ErrorDescriptor> errorDescriptors;
        try {
            errorDescriptors = analyzer.getAllErrors();
        } catch (KotlinCoreException e) {
            return ResponseUtils.getErrorWithStackTraceInJson(ApplicationSettings.KOTLIN_ERROR_MESSAGE
                     , e.getStackTraceString());
        }
        ArrayNode resultArray = new ArrayNode(JsonNodeFactory.instance);

        for (ErrorDescriptor errorDescriptor : errorDescriptors) {
            resultArray.add(getMapForJsonResponse(errorDescriptor.getInterval(), errorDescriptor.getMessage(),
                    errorDescriptor.getClassName(), errorDescriptor.getSeverity().name()));
        }
        return ResponseUtils.escapeString(resultArray.toString());
    }

    @NotNull
    private ObjectNode getMapForJsonResponse(Interval interval, String titleName, String className, String severity) {
        ObjectNode jsonObject = new ObjectNode(JsonNodeFactory.instance);

        jsonObject.put("x", "{line: " + interval.startPoint.line + ", ch: " + interval.startPoint.charNumber + "}");
        jsonObject.put("y", "{line: " + interval.endPoint.line + ", ch: " + interval.endPoint.charNumber + "}");
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
