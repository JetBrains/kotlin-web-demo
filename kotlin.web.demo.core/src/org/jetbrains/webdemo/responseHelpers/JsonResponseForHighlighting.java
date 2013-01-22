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

import org.jetbrains.jet.internal.com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.webdemo.Interval;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.errorsDescriptors.ErrorAnalyzer;
import org.jetbrains.webdemo.errorsDescriptors.ErrorDescriptor;
import org.jetbrains.webdemo.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.server.ApplicationSettings;
import org.jetbrains.webdemo.session.SessionInfo;
import org.json.JSONArray;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        JSONArray resultArray = new JSONArray();

        for (ErrorDescriptor errorDescriptor : errorDescriptors) {
            resultArray.put(getMapForJsonResponse(errorDescriptor.getInterval(), errorDescriptor.getMessage(),
                    errorDescriptor.getClassName(), errorDescriptor.getSeverity().name()));
        }
        return ResponseUtils.escapeString(resultArray.toString());
    }

    @NotNull
    private Map<String, String> getMapForJsonResponse(Interval interval, String titleName, String className, String severity) {
        Map<String, String> map = new HashMap<String, String>();

        map.put("x", "{line: " + interval.startPoint.line + ", ch: " + interval.startPoint.charNumber + "}");
        map.put("y", "{line: " + interval.endPoint.line + ", ch: " + interval.endPoint.charNumber + "}");
        map.put("titleName", escape(titleName));
        map.put("className", className);
        map.put("severity", severity);
        return map;
    }


    private String escape(String str) {
        if ((str != null) && (str.contains("\""))) {
            str = str.replaceAll("\\\"", "'");
        }
        return str;
    }

}
