package org.jetbrains.webdemo.backend;/*
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

import com.intellij.psi.PsiFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by atamas on 26.02.15.
 */
public class BackendUtils {
    public static Map<String, String> getPsiFilesContent(List<PsiFile> psiFiles){
        Map<String, String> result = new HashMap<>();
        for(PsiFile psiFile : psiFiles){
            result.put(psiFile.getName(), psiFile.getText());
        }
        return result;
    }
}
