/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package org.jetbrains.webdemo.kotlin.datastructures;

import java.util.HashMap;
import java.util.Map;

public class MethodPositions {
    private Map<String, ClassMethodPositions> classMethodPositions = new HashMap<>();

    public void addClassMethodPositions(String classFileName, ClassMethodPositions methodPositions){
        String className = classFileName.substring(0, classFileName.lastIndexOf('.'));
        classMethodPositions.put(className, methodPositions);
    }

    public int getMethodPosition(String className, String methodName) {
        if(!classMethodPositions.containsKey(className)) {
            return -1;
        }
        return classMethodPositions.get(className).getMethodPosition(methodName);
    }

    public String getSourceFile(String className) {
        if(!classMethodPositions.containsKey(className)) {
            return null;
        }
        return classMethodPositions.get(className).getSourceFile();
    }
}
