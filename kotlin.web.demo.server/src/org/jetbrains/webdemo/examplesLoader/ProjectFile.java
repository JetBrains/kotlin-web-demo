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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.intellij.psi.PsiFile;

/**
 * Created by Semyon.Atamas on 8/11/2014.
 */
public class ProjectFile {


    private Boolean modifiable;
    private String publicId;
    private String text;
    private String name;
    private Type type;

    private PsiFile psiFile;

    /**
     * This constructor is used to deserialize project file from user request. Jackson calls it wia
     * reflection when we using it to deserialize user project
     *
     * @param name    - File name
     * @param text - File content
     */
    @JsonCreator
    public ProjectFile(@JsonProperty("name") String name,
                       @JsonProperty("text") String text,
                       @JsonProperty("publicId") String publicId) {
        this.name = name;
        this.text = text;
        this.publicId = publicId;
        this.modifiable = true;
        this.type = Type.KOTLIN_FILE;
    }


    public ProjectFile(String name, String text, boolean modifiable, String publicId, Type type) {
        this.name = name;
        this.text = text;
        this.modifiable = modifiable;
        this.publicId = publicId;
        this.type = type;
//        psiFile = JetPsiFactoryUtil.createFile(Initializer.INITIALIZER.getEnvironment().getProject(), name, content);
//        type = psiFile.getFileType().getDescription();
    }

    public Boolean isModifiable() {
        return modifiable;
    }

    public String getText() {
        return text;
    }

    public String getName() {
        return name;
    }

    public Type getType() {
        return type;
    }

    public String getPublicId() {
        return publicId;
    }

    public enum Type {
        KOTLIN_FILE,
        KOTLIN_TEST_FILE,
        SOLUTION_FILE
    }
}
