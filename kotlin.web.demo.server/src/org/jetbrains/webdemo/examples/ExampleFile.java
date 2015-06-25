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
import org.jetbrains.annotations.Nullable;
import org.jetbrains.webdemo.ProjectFile;

public class ExampleFile extends ProjectFile{
    private boolean hidden;
    @Nullable
    private String confType;

    public ExampleFile(String name, String text, String id, Type type, @Nullable String confType, boolean isModifiable, boolean isHidden){
        super(name, text, isModifiable, id, type);
        hidden = isHidden;
        this.confType = confType;
    }

    @JsonIgnore
    public boolean isHidden(){
        return hidden;
    }

    @JsonIgnore @Nullable
    public String getConfType() {
        return confType;
    }
}

class TaskFile extends ExampleFile{
    public TaskFile(String text, String id) {
        super("Task.kt", text, id, Type.KOTLIN_FILE, null, true, false);
    }
}

class TestFile extends ExampleFile{
    public TestFile(String text, String id) {
        super("Test.kt", text, id, Type.KOTLIN_TEST_FILE, null, false, false);
    }
}

class SolutionFile extends ExampleFile{
    public SolutionFile(String text, String id) {
        super("Solution.kt", text, id, Type.SOLUTION_FILE, null, false, false);
    }
}
