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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.psi.PsiFile;
import org.jetbrains.kotlin.diagnostics.Severity;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.JsonUtils;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.backend.BackendSessionInfo;
import org.jetbrains.webdemo.backend.BackendSettings;
import org.jetbrains.webdemo.backend.BackendUtils;
import org.jetbrains.webdemo.backend.Initializer;
import org.jetbrains.webdemo.backend.errorsDescriptors.ErrorAnalyzer;
import org.jetbrains.webdemo.backend.errorsDescriptors.ErrorDescriptor;
import org.jetbrains.webdemo.backend.exceptions.KotlinCoreException;
import org.jetbrains.webdemo.backend.translator.WebDemoTranslatorFacade;

import java.util.List;
import java.util.Map;

public class JsConverter {
    private final BackendSessionInfo info;

    public JsConverter(BackendSessionInfo info) {
        this.info = info;
    }

    public String getResult(List<PsiFile> files, BackendSessionInfo sessionInfo, String arguments) {
        ErrorAnalyzer analyzer = new ErrorAnalyzer(files, sessionInfo, Initializer.getInstance().getEnvironment().getProject());
        Map<String, List<ErrorDescriptor>> errors;
        try {
            errors = analyzer.getAllErrors();
        } catch (KotlinCoreException e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e, sessionInfo.getType(), sessionInfo.getOriginUrl(), BackendUtils.getPsiFilesContent(files));
            return ResponseUtils.getErrorWithStackTraceInJson(BackendSettings.KOTLIN_ERROR_MESSAGE, e.getStackTraceString());
        }

        ObjectNode response = new ObjectNode(JsonNodeFactory.instance);
        response.put("errors", JsonUtils.getObjectMapper().valueToTree(errors));
        if (isOnlyWarnings(errors)) {
            response.put("code", WebDemoTranslatorFacade.translateProjectWithCallToMain((List) files, arguments, info));
        }
        return response.toString();
    }

    private boolean isOnlyWarnings(Map<String, List<ErrorDescriptor>> map) {
        for (String key : map.keySet()) {
            for (ErrorDescriptor errorDescriptor : map.get(key)) {
                if (errorDescriptor.getSeverity() == Severity.ERROR) {
                    return false;
                }
            }
        }
        return true;
    }
}
