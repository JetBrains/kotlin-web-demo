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
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.webdemo.server.ApplicationSettings;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Semyon.Atamas on 8/11/2014.
 */
public class ExamplesFolder {
    private static ObjectMapper objectMapper = new ObjectMapper();
    public String name;
    Map<String, ExampleObject> examples = new HashMap<>();
    private String path;

    public Collection getExamplesList(){
        return examples.values();
    }

    @JsonCreator
    ExamplesFolder(@JsonProperty("folder") String folderName, @JsonProperty("examples") String[] exampleNames) {
        path = ApplicationSettings.EXAMPLES_DIRECTORY + File.separator + folderName;
        for (String exampleName : exampleNames) {
            try {
                ExampleObject example = downloadExample(exampleName);
                examples.put(exampleName, example);
            } catch (Exception e) {
                System.err.println("Can't load example " + exampleName + ":\n" + e.getMessage());
            }
        }
    }

    private ExampleObject downloadExample(String exampleName) throws IOException {
        File manifest = new File(path + File.separator + exampleName + File.separator + "manifest.json");
        return objectMapper.readValue(manifest, ExampleObject.class);
    }
}
