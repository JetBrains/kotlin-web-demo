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

package org.jetbrains.webdemo.examples;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.Project;
import org.jetbrains.webdemo.ProjectFile;

import java.util.ArrayList;
import java.util.List;

public class Example extends Project{
    private List<ProjectFile> hiddenFiles = new ArrayList<>();
    @JsonIgnore
    public ExamplesFolder parent;
    private String help;
    private boolean searchForMain;

    public Example(
            String id,
            String name,
            String args,
            String confType,
            String originUrl,
            String expectedOutput,
            boolean searchForMain,
            List<ProjectFile> files,
            List<ProjectFile> hiddenFiles,
            List<String> readOnlyFileNames,
            String help) {
        super(id, name, args, confType, originUrl, expectedOutput, files, readOnlyFileNames);
        this.hiddenFiles = hiddenFiles;
        this.help = help;
        this.searchForMain = searchForMain;
    }

    @JsonIgnore
    public List<ProjectFile> getHiddenFiles(){
        return hiddenFiles;
    }

    @JsonProperty("searchForMain")
    public boolean shouldSearchForMain(){
        return searchForMain;
    }

    public String getHelp() {
        return help;
    }

    public String toString(){
        return name + " (" + id.replaceAll("%20", " ") + ')';
    }
}