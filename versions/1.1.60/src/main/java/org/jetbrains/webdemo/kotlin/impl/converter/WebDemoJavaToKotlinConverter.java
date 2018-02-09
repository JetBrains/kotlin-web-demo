/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package org.jetbrains.webdemo.kotlin.impl.converter;

import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import org.jetbrains.kotlin.j2k.ConverterSettings;
import org.jetbrains.kotlin.j2k.EmptyJavaToKotlinServices;
import org.jetbrains.kotlin.j2k.JavaToKotlinConverter;
import org.jetbrains.kotlin.j2k.JavaToKotlinTranslator;
import org.jetbrains.webdemo.kotlin.impl.ResolveUtils;
import org.jetbrains.webdemo.kotlin.impl.environment.EnvironmentManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class WebDemoJavaToKotlinConverter {
    public static String getResult(String code) {
        Project project = EnvironmentManager.getEnvironment().getProject();
        JavaToKotlinConverter converter = new JavaToKotlinConverter(
                project,
                ConverterSettings.Companion.getDefaultSettings(),
                EmptyJavaToKotlinServices.INSTANCE);
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
            for (PsiElement element : psiClass.getChildren()) {
                if (element instanceof PsiErrorElement) {
                    errorsFound = true;
                }
            }
            if (!errorsFound) {
                inputElements = Arrays.asList(psiClass.getChildren());
            }
        }

        if (inputElements == null) {
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
        textResult = prettify(textResult);
        return textResult;
    }

    private static String prettify(String code) {
        try {
            Method method = JavaToKotlinTranslator.INSTANCE.getClass().getDeclaredMethod("prettify", String.class);
            method.setAccessible(true);
            return (String) method.invoke(JavaToKotlinTranslator.INSTANCE, code);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }


}

