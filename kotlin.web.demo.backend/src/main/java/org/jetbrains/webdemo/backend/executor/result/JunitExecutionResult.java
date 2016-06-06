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

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import junit.framework.TestResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JunitExecutionResult extends ExecutionResult {
    private Map<String, List<TestRunInfo>> testResults;

    public JunitExecutionResult(@JsonProperty("testResults") Map<String, List<TestRunInfo>> testResults) {
        this.testResults = new HashMap<>();
    }

    @JsonAnySetter
    public void addClassTests(String className, List<TestRunInfo> testRunInfo) {
        testResults.put(className, testRunInfo);
    }

    public Map<String, List<TestRunInfo>> getTestResults() {
        return testResults;
    }
}

class ComparisonFailureDescriptor extends ExceptionDescriptor {
    private String expected;
    private String actual;

    ComparisonFailureDescriptor(
            @JsonProperty("message") String message,
            @JsonProperty("fullName") String fullName,
            @JsonProperty("stackTrace") List<StackTraceElement> stackTrace,
            @JsonProperty("cause") ExceptionDescriptor cause,
            @JsonProperty("expected") String expected,
            @JsonProperty("actual") String actual
    ) {
        super(message, fullName, stackTrace, cause);
        this.expected = expected;
        this.actual = actual;
    }

    public String getExpected() {
        return expected;
    }

    public String getActual() {
        return actual;
    }
}
