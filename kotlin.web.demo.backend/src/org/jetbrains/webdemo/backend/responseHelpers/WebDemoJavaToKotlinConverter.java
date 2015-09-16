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

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.kotlin.j2k.*;
import org.jetbrains.kotlin.resolve.jvm.TopDownAnalyzerFacadeForJVM;
import org.jetbrains.webdemo.ErrorWriter;
import org.jetbrains.webdemo.ResponseUtils;
import org.jetbrains.webdemo.backend.BackendSessionInfo;
import org.jetbrains.webdemo.backend.ResolveUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WebDemoJavaToKotlinConverter {
    private final BackendSessionInfo info;

    public WebDemoJavaToKotlinConverter(BackendSessionInfo info) {
        this.info = info;
    }

    public String getResult(String code, Project project) {
        ArrayNode result = new ArrayNode(JsonNodeFactory.instance);

        try {
            JavaToKotlinConverter converter = new JavaToKotlinConverter(
                    project,
                    ConverterSettings.defaultSettings,
                    EmptyJavaToKotlinServices.INSTANCE$);
            PsiElementFactory instance = PsiElementFactory.SERVICE.getInstance(project);

            List<PsiElement> inputElements = null;
            PsiFile javaFile = PsiFileFactory.getInstance(project).createFileFromText("test.java", JavaLanguage.INSTANCE, code);

            //To create a module
            ResolveUtils.getBindingContext(Collections.EMPTY_LIST, project, false);

            for (PsiElement element : javaFile.getChildren()) {
                if (element instanceof PsiClass) {
                    inputElements = Collections.<PsiElement>singletonList(javaFile);
                }
            }

            if (inputElements == null) {
                PsiClass psiClass = instance.createClassFromText(code, javaFile);
                boolean errorsFound = false;
                for(PsiElement element : psiClass.getChildren()){
                    if(element instanceof PsiErrorElement){
                        errorsFound = true;
                    }
                }
                if(!errorsFound) {
                    inputElements = Arrays.asList(psiClass.getChildren());
                }
            }

            if(inputElements == null){
                PsiCodeBlock codeBlock = instance.createCodeBlockFromText("{" + code + "}", javaFile);
                PsiElement[] childrenWithoutBraces = Arrays.copyOfRange(codeBlock.getChildren(), 1, codeBlock.getChildren().length - 1);
                inputElements = Arrays.asList(childrenWithoutBraces);
            }

            List<JavaToKotlinConverter.ElementResult> resultFormConverter = converter.elementsToKotlin(inputElements).getResults();
            String textResult = "";
            for (JavaToKotlinConverter.ElementResult it : resultFormConverter) {
                if (it == null) continue;
                textResult = textResult + it.getText() + "\n";
            }
            textResult = JavaToKotlinTranslator.INSTANCE$.prettify(textResult);

            if (resultFormConverter.isEmpty()) {
                result.add(ResponseUtils.getErrorAsJsonNode("Generated code is empty."));
            } else {
                ObjectNode jsonObject = result.addObject();
                jsonObject.put("text", textResult);
            }
        } catch (Throwable e) {
            ErrorWriter.ERROR_WRITER.writeExceptionToExceptionAnalyzer(e,
                    BackendSessionInfo.TypeOfRequest.CONVERT_TO_KOTLIN.name(), info.getOriginUrl(), code);
            if (e.getMessage() != null) {
                result.add(ResponseUtils.getErrorAsJsonNode("EXCEPTION: " + e.getMessage()));
            } else {
                result.add(ResponseUtils.getErrorAsJsonNode("Unknown exception"));
            }
        }

        return result.toString();
    }

}
