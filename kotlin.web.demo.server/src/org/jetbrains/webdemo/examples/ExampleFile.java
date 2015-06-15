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
import org.jetbrains.webdemo.ProjectFile;

public class ExampleFile extends ProjectFile{
    private boolean hidden;

    public ExampleFile(String name, String text, String id, Type type, boolean isModifiable, boolean isHidden){
        super(name, text, isModifiable, id, type);
        hidden = isHidden;
    }

    @JsonIgnore
    public boolean isHidden(){
        return hidden;
    }
}
