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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.webdemo.ApplicationSettings;
import org.jetbrains.webdemo.Project;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Semyon.Atamas on 8/11/2014.
 */
public class ExamplesFolder {

    public String name;
    public List<String> examplesOrder = new ArrayList<>();
    @JsonIgnore public Map<String, Project> examples = new HashMap<>();
    private String path;

    public ExamplesFolder(String name){
        this.name =name;
    }

    @JsonCreator
    ExamplesFolder(@JsonProperty("folder") String folderName, @JsonProperty("examples") String[] exampleNames) {
        path = ApplicationSettings.EXAMPLES_DIRECTORY + File.separator + folderName;
        for (String exampleName : exampleNames) {
            try {
                Project example = new Project(folderName, exampleName, ApplicationSettings.EXAMPLES_DIRECTORY, ApplicationSettings.LOAD_TEST_VERSION_OF_EXAMPLES);
                example.parent = folderName;
                examplesOrder.add(example.name);
                examples.put(exampleName, example);
            } catch (Exception e) {
                System.err.println("Can't load example " + exampleName + ":\n" + e.getMessage());
            }
        }
    }

    public List<String> getOrderedExampleNames() {
        return examplesOrder;
    }
}
