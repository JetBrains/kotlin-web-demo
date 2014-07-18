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

package org.jetbrains.webdemo.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by atamas on 18.07.14.
 */
public class ExecResult {
    private String stdErr;
    private List<StackTraceLine> stackTraceLines = new ArrayList<StackTraceLine>();

    public ExecResult(String stdErr, List<StackTraceLine> stackTraceLines){
        this.stdErr = stdErr;
        this.stackTraceLines = stackTraceLines;
    }

    public String getStdErr() {
        return stdErr;
    }


    public List<StackTraceLine> getStackTraceLines() {
        return Collections.unmodifiableList(stackTraceLines);
    }

}
