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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by atamas on 16.07.14.
 */
public class StackTraceParser {


    public static ExecResult parseStackTraceElements(String input) {
        ExecResult execResult = new ExecResult();

        StringBuilder result = new StringBuilder();
        String rest = input;
        Pattern tracePattern = Pattern.compile("(.*?)<br/>\\s*at\\s+(.*?)\\((.*?):([0-9]+)\\)");
        Matcher traceLineMatcher = tracePattern.matcher(rest);
        while (traceLineMatcher.find()) {
            StackTraceElement stackTraceElement = new StackTraceElement();
            result.append(traceLineMatcher.group(1));
            result.append("<br/>%STACK_TRACE_LINE%");
            stackTraceElement.setLocation(traceLineMatcher.group(2));
            stackTraceElement.setFile(traceLineMatcher.group(3));
            stackTraceElement.setLineNo(traceLineMatcher.group(4));
            execResult.addStackTarceLine(stackTraceElement);
            rest = rest.substring(traceLineMatcher.group().length());
            traceLineMatcher = tracePattern.matcher(rest);
        }
        result.append(rest);
        execResult.setStdErr(result.toString());
        return execResult;
    }

}




