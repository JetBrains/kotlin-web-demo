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

package org.jetbrains.webdemo;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Semyon.Atamas on 8/11/2014.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProjectFile {


    protected Boolean modifiable;
    protected String publicId;
    protected String text;
    protected String name;
    protected Type type;

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
    }

    public ProjectFile(ProjectFile other) {
        this.name = other.name;
        this.text = other.text;
        this.modifiable = other.modifiable;
        this.publicId = other.publicId;
        this.type = other.type;
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
        SOLUTION_FILE,
        JAVA_FILE
    }
}
