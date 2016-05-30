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

package org.jetbrains.webdemo.test.examples;

import com.fasterxml.jackson.databind.node.ArrayNode;
import junit.framework.Test;
import junit.framework.TestResult;
import junit.framework.TestSuite;
import org.jetbrains.webdemo.ProjectFile;
import org.jetbrains.webdemo.backend.executor.ExecutorUtils;
import org.jetbrains.webdemo.backend.executor.result.ExecutionResult;
import org.jetbrains.webdemo.backend.executor.result.JavaExecutionResult;
import org.jetbrains.webdemo.backend.executor.result.JunitExecutionResult;
import org.jetbrains.webdemo.backend.executor.result.TestRunInfo;
import org.jetbrains.webdemo.examples.Example;
import org.jetbrains.webdemo.examples.ExamplesFolder;
import org.jetbrains.webdemo.examples.ExamplesUtils;
import org.jetbrains.webdemo.kotlin.datastructures.CompilationResult;
import org.jetbrains.webdemo.test.BaseTest;
import org.jetbrains.webdemo.test.Common;
import org.jetbrains.webdemo.test.TestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunExamplesTest extends BaseTest {

    private static List<String> jsExamples = new ArrayList<String>();
    private final Example project;
    private final String runConfiguration;

    public RunExamplesTest(Example project) {
        super(project.name);
        this.project = project;
        this.runConfiguration = project.confType;
    }

    public RunExamplesTest(Example project, String runConfiguration) {
        super(project.name + "_" + runConfiguration);
        this.project = project;
        this.runConfiguration = runConfiguration;
    }

    public static Test suite() {
        jsExamples.add("is-checks and smart casts");
        jsExamples.add("Use a while-loop");
        jsExamples.add("Use a for-loop");
        jsExamples.add("Simplest version");
        jsExamples.add("Reading a name from the command line");
        jsExamples.add("Reading many names from the command line");
        jsExamples.add("A multi-language Hello");
        jsExamples.add("An object-oriented Hello");
        //jsExamples.add("HTML Builder.kt");

        TestSuite suite = new TestSuite(RunExamplesTest.class.getName());
        for (Example project : ExamplesUtils.getAllExamples(ExamplesFolder.ROOT_FOLDER)) {
            suite.addTest(new RunExamplesTest(project));
            if (jsExamples.contains(project.name)) {
                suite.addTest(new RunExamplesTest(project, "js"));
            }
        }
        return suite;
    }


    @Override
    protected void runTest() throws Throwable {
        Map<String, String> files = new HashMap<>();
        for (ProjectFile file : project.files) {
            files.put(file.getName(), file.getText());
        }
        for (ProjectFile file : project.getHiddenFiles()) {
            files.put(file.getName(), file.getText());
        }

        if (project.confType.equals("java")) {
            JavaExecutionResult executionResult = (JavaExecutionResult) compileAndExecute(files, project.args, false);
            assertEquals(unifyLineSeparators(project.expectedOutput), getStdOut(executionResult.getText()));
            assertNull(executionResult.getException());
        } else if (project.confType.equals("junit")) {
            JunitExecutionResult executionResult = (JunitExecutionResult) compileAndExecute(files, project.args, true);
            assertFalse("No test results", executionResult.getTestResults().isEmpty());
            for (TestRunInfo testRunInfo : executionResult.getTestResults()) {
                String message = testRunInfo.getClassName() + "." + testRunInfo.getMethodName() + " status:" + testRunInfo.getStatus();
                assertEquals(message, TestRunInfo.Status.OK, testRunInfo.getStatus());
            }
        }

    }

    private String getStdOut(String programOutput) {
        programOutput = Common.unEscapeString(programOutput).replaceAll("\\r\\n", "</br>").replaceAll("\\n", "</br>");
        StringBuilder builder = new StringBuilder();
        Matcher matcher = Pattern.compile("<outStream>(.*)</outStream>").matcher(programOutput);
        while (matcher.find()) {
            builder.append(matcher.group(1));
        }
        return unifyLineSeparators(builder.toString());
    }

    private ExecutionResult compileAndExecute(Map<String, String> files, String args, boolean isJunit) throws Exception {
        CompilationResult compilationResult = kotlinWrapper.compileCorrectFiles(files);
        return ExecutorUtils.executeCompiledFiles(
                compilationResult.getFiles(),
                compilationResult.getMainClass(),
                kotlinWrapper.getKotlinRuntimeLibraries(),
                kotlinWrapper.getKotlinCompilerJar(),
                args,
                isJunit
        );
    }

    private String unifyLineSeparators(String text) {
        return text.replaceAll("\\r\\n", "</br>").replaceAll("\\n", "</br>").replaceAll("</br>", System.lineSeparator());
    }
}

