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

package org.jetbrains.webdemo.backend.executor.result;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class JavaExecutionResult extends ExecutionResult {
    private String text;
    private ExceptionDescriptor exception;

    @JsonCreator
    public JavaExecutionResult(
            @JsonProperty("text") String text,
            @JsonProperty("exception") ExceptionDescriptor exception
    ) {
        this.text = text;
        this.exception = exception;
    }

    public String getText() {
        return text;
    }

    public ExceptionDescriptor getException() {
        return exception;
    }
}

class ExceptionDescriptor {
    private String message;
    private String fullName;
    private List<StackTraceElement> stackTrace;
    private ExceptionDescriptor cause;

    @JsonCreator
    ExceptionDescriptor(
            @JsonProperty("message") String message,
            @JsonProperty("fullName") String fullName,
            @JsonProperty("stackTrace") List<StackTraceElement> stackTrace,
            @JsonProperty("cause") ExceptionDescriptor cause
    ) {
        this.message = message;
        this.fullName = fullName;
        this.stackTrace = stackTrace;
        this.cause = cause;
    }
}

class StackTraceElement {
    private String declaringClass;
    private String methodName;
    private String fileName;
    private int lineNumber;

    @JsonCreator
    public StackTraceElement(
            @JsonProperty("declaringClass") String declaringClass,
            @JsonProperty("methodName") String methodName,
            @JsonProperty("fileName") String fileName,
            @JsonProperty("lineNumber") int lineNumber
    ) {
        this.declaringClass = declaringClass;
        this.methodName = methodName;
        this.fileName = fileName;
        this.lineNumber = lineNumber;
    }
}
