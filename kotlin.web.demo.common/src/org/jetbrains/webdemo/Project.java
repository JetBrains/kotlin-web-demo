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


import java.util.ArrayList;
import java.util.List;

public class Project {
    public String originUrl;
    public String name;
    public String args = "";
    public String confType = "java";

    public String help = "";
    public List<ProjectFile> files;
    public List<String> readOnlyFileNames = new ArrayList<>();
    public String expectedOutput;

    /**
     * For Jackson
     */
    public Project() {

    }

    public Project( String name,
                    String args,
                    String confType) {
        this.name = name;
        this.args = args;
        this.confType = confType;
        this.originUrl = null;
        this.files = new ArrayList<>();
    }

    public Project( String name,
                    String args,
                    String confType,
                   String originUrl,
                   List<String> readOnlyFileNames) {
        this.name = name;
        this.args = args;
        this.confType = confType;
        this.originUrl = originUrl;
        this.files = new ArrayList<>();
        this.readOnlyFileNames = readOnlyFileNames;
    }


    public Project(String name,
                   String args,
                   String confType,
                   String originUrl,
                   String expectedOutput,
                   String help,
                   List<ProjectFile> files,
                   List<String> readOnlyFileNames) {
        this.name = name;
        this.args = args;
        this.confType = confType;
        this.originUrl = originUrl;
        this.files = files;
        this.expectedOutput = expectedOutput;
        this.help = help;
        this.readOnlyFileNames = readOnlyFileNames;
    }
}
