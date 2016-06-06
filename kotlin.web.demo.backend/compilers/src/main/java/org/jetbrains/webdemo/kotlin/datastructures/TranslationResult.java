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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TranslationResult {
    private String jsCode;
    private Map<String, List<ErrorDescriptor>> errors;


    public TranslationResult(String jsCode) {
        this.jsCode = jsCode;
        this.errors = new HashMap<>();
    }

    public TranslationResult(Map<String, List<ErrorDescriptor>> errors) {
        this.errors = errors;
    }

    public String getJsCode() {
        return jsCode;
    }

    public void addWarningsFromAnalyzer(Map<String, List<ErrorDescriptor>> warnings) {
        for (String fileName : warnings.keySet()) {
            if (!errors.containsKey(fileName)) {
                errors.put(fileName, new ArrayList<ErrorDescriptor>());
            }

            for(ErrorDescriptor warning : warnings.get(fileName)){
                errors.get(fileName).add(warning);
            }
        }
    }

    public Map<String, List<ErrorDescriptor>> getErrors() {
        return errors;
    }

}
