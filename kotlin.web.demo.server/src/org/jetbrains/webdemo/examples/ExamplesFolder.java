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

import org.jetbrains.webdemo.Project;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Semyon.Atamas on 8/11/2014.
 */
public class ExamplesFolder {
    public static ExamplesFolder ROOT_FOLDER;
    private String id;
    private String name;
    private Map<String, Example> examples;
    private Map<String, ExamplesFolder> childFolders;

    public ExamplesFolder(String name,
                          String id,
                          Map<String, Example> examples,
                          Map<String, ExamplesFolder> childFolders) {
        this.name = name;
        this.id = id;
        this.examples = examples;
        this.childFolders = childFolders;
    }

    public Collection<ExamplesFolder> getChildFolders() {
        return childFolders.values();
    }

    public ExamplesFolder getChildFolder(String name) {
        return childFolders.get(name);
    }

    public Collection<Example> getExamples() {
        return examples.values();
    }

    public Example getExample(String name) {
        return examples.get(name);
    }

    public String getName() {
        return name;
    }

    public String getId(){
        return id;
    }
}
