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

import com.fasterxml.jackson.annotation.JsonProperty;

public class TestRunInfo {
    private String output;
    private String sourceFileName;
    private String className;
    private String methodName;
    private long executionTime;
    private ExceptionDescriptor exception;
    private ComparisonFailureDescriptor comparisonFailure;
    private int methodPosition;
    private Status status;

    public TestRunInfo(
            @JsonProperty("output") String output,
            @JsonProperty("className") String className,
            @JsonProperty("methodName") String methodName,
            @JsonProperty("executionTime") long executionTime,
            @JsonProperty("exception") ExceptionDescriptor exception,
            @JsonProperty("comparisonFailure") ComparisonFailureDescriptor comparisonFailure,
            @JsonProperty("status") Status status
    ) {
        this.output = output;
        this.className = className;
        this.methodName = methodName;
        this.executionTime = executionTime;
        this.exception = exception;
        this.status = status;
        this.comparisonFailure = comparisonFailure;
    }

    public String getOutput() {
        return output;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public ExceptionDescriptor getException() {
        return exception;
    }

    public int getMethodPosition() {
        return methodPosition;
    }

    public Status getStatus() {
        return status;
    }

    public ComparisonFailureDescriptor getComparisonFailure() {
        return comparisonFailure;
    }

    public void setSourceFileName(String sourceFileName) {
        this.sourceFileName = sourceFileName;
    }

    public void setMethodPosition(int methodPosition) {
        this.methodPosition = methodPosition;
    }

    public enum Status {
        OK,
        FAIL,
        ERROR
    }
}
