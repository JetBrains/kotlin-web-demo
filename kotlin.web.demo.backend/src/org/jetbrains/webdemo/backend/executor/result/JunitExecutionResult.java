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

import java.util.List;

public class JunitExecutionResult extends ExecutionResult {
    private List<TestRunInfo> testResults;

    public JunitExecutionResult(@JsonProperty("testResults") List<TestRunInfo> testResults) {
        this.testResults = testResults;
    }
}

class TestRunInfo {
    public String output = "";
    public String sourceFileName = "";
    public String className = "";
    public String methodName = "";
    public long executionTime = 0;
    public ExceptionDescriptor exception = null;
    public int methodPosition;
    public Status status = Status.OK;

    public enum Status {
        OK,
        FAIL,
        ERROR
    }
}
