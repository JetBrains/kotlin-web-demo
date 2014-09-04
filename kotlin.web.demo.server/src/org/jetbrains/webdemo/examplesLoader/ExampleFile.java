/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package org.jetbrains.webdemo.examplesLoader;

import com.intellij.psi.PsiFile;
import org.jetbrains.webdemo.Initializer;
import org.jetbrains.webdemo.JetPsiFactoryUtil;

/**
 * Created by Semyon.Atamas on 8/11/2014.
 */

public class ExampleFile {


    private Boolean modifiable;
    private String content;
    private String name;
    private String type;

    private PsiFile psiFile;


    public ExampleFile(String name, String content, boolean modifiable) {
        this.name = name;
        this.content = content;
        this.modifiable = modifiable;
        psiFile = JetPsiFactoryUtil.createFile(Initializer.INITIALIZER.getEnvironment().getProject(), name, content);
        type = psiFile.getFileType().getDescription();
    }

    public Boolean getModifiable() {
        return modifiable;
    }

    public String getContent() {
        return content;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public enum Type {
        DATA_FILE,
        TEST_FILE,
        SOURCE_FILE
    }
}
