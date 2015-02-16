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

import com.intellij.psi.PsiFile;
import org.jetbrains.kotlin.psi.JetFile;
import org.jetbrains.webdemo.backend.BackendSessionInfo;
import org.jetbrains.webdemo.backend.translator.WebDemoTranslatorFacade;

import java.util.ArrayList;
import java.util.List;

public class JsConverter {
    private final BackendSessionInfo info;

    public JsConverter(BackendSessionInfo info) {
        this.info = info;
    }

    public String getResult(List<PsiFile> files, String arguments) {
        return WebDemoTranslatorFacade.translateProjectWithCallToMain(convertList(files), arguments, info);
    }

    private List<JetFile> convertList(List<PsiFile> list) {
        List<JetFile> ans = new ArrayList<>();
        for (PsiFile psiFile : list) {
            ans.add((JetFile) psiFile);
        }
        return ans;
    }
}
