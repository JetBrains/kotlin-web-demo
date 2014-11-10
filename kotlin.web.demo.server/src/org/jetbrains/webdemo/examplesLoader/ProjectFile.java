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
    private String content;
    private String name;
    private String type;

    private PsiFile psiFile;

    /**
     * This constructor is used to deserialize project file from user request. Jackson calls it wia
     * reflection when we using it to deserialize user project
     *
     * @param name    - File name
     * @param content - File content
     */
    @JsonCreator
    public ProjectFile(@JsonProperty("name") String name,
                       @JsonProperty("text") String content,
                       @JsonProperty("publicId") String publicId) {
        this.name = name;
        this.content = content;
        this.publicId = publicId;
        this.modifiable = true;
    }


    public ProjectFile(String name, String content, boolean modifiable, String publicId) {
        this.name = name;
        this.content = content;
        this.modifiable = modifiable;
        this.publicId = publicId;
        this.type = "Kotlin";
//        psiFile = JetPsiFactoryUtil.createFile(Initializer.INITIALIZER.getEnvironment().getProject(), name, content);
//        type = psiFile.getFileType().getDescription();
    }

    public Boolean isModifiable() {
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

    public String getPublicId() {
        return publicId;
    }

    public enum Type {
        DATA_FILE,
        TEST_FILE,
        SOURCE_FILE
    }
}
