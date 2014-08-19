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
import org.jetbrains.webdemo.server.ApplicationSettings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by Semyon.Atamas on 8/11/2014.
 */
public class ExampleFile {
    public enum Type {
        DATA_FILE,
        TEST_FILE,
        SOURCE_FILE
    }

    public Type type;

    public String content;

    public String name;

    @JsonCreator
    public ExampleFile(@JsonProperty("filename") String filename) throws IOException {
        if(filename != null) {
            Path path = Paths.get(ApplicationSettings.EXAMPLES_DIRECTORY + File.separator + filename);
            if (name == null) {
                name = path.getFileName().toString();
            }
            content = new String(Files.readAllBytes(path)).replaceAll(System.lineSeparator(), "\n");
        }
    }
}
