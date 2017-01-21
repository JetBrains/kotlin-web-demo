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

import org.jetbrains.webdemo.ProjectFile;
import org.jetbrains.webdemo.backend.executor.ExecutorUtils;
import org.jetbrains.webdemo.backend.executor.result.ExecutionResult;
import org.jetbrains.webdemo.backend.executor.result.JavaExecutionResult;
import org.jetbrains.webdemo.backend.executor.result.JunitExecutionResult;
import org.jetbrains.webdemo.backend.executor.result.TestRunInfo;
import org.jetbrains.webdemo.examples.Example;
import org.jetbrains.webdemo.examples.ExamplesFolder;
import org.jetbrains.webdemo.examples.ExamplesUtils;
import org.jetbrains.webdemo.kotlin.KotlinWrapper;
import org.jetbrains.webdemo.kotlin.KotlinWrappersManager;
import org.jetbrains.webdemo.kotlin.datastructures.CompilationResult;
import org.jetbrains.webdemo.kotlin.datastructures.ErrorDescriptor;
import org.jetbrains.webdemo.kotlin.datastructures.TextPosition;
import org.jetbrains.webdemo.test.BaseTest;
import org.jetbrains.webdemo.test.Common;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(Parameterized.class)
public class ExamplesTest extends BaseTest {
    private final Example project;

    public ExamplesTest(Example project) {
        super(KotlinWrappersManager.INSTANCE.getDefaultWrapper().getWrapperVersion(), project.name);
        this.project = project;
    }

    @Test
    public void testHighlighting() throws Throwable {
        Map<String, String> files = new HashMap<>();
        for (ProjectFile file : project.files) {
            files.put(file.getName(), file.getText());
        }
        for (ProjectFile file : project.getHiddenFiles()) {
            files.put(file.getName(), file.getText());
        }

        boolean isJs = project.confType.equals("js") || project.confType.equals("canvas");
        Map<String, List<ErrorDescriptor>> result = kotlinWrapper.getErrors(files, isJs);
        for (String fileName : result.keySet()) {
            assertTrue(getErrorsMessages(fileName, result.get(fileName)), result.get(fileName).isEmpty());
        }
    }

    @Test
    public void testRun() throws Exception {
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
            for (String className : executionResult.getTestResults().keySet()) {
                for (TestRunInfo testRunInfo : executionResult.getTestResults().get(className)) {
                    String message = testRunInfo.getClassName() + "." + testRunInfo.getMethodName() + " status:" + testRunInfo.getStatus();
                    assertEquals(message, TestRunInfo.Status.OK, testRunInfo.getStatus());
                }
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
        CompilationResult compilationResult = kotlinWrapper.compileCorrectFiles(files, null);
        return ExecutorUtils.executeCompiledFiles(
                compilationResult.getFiles(),
                compilationResult.getMainClass(),
                kotlinWrapper.getKotlinRuntimeLibraries(),
                args,
                kotlinWrapper.getWrapperFolder().resolve("executors.policy"),
                isJunit
        );
    }

    private String unifyLineSeparators(String text) {
        return text.replaceAll("\\r\\n", "</br>").replaceAll("\\n", "</br>").replaceAll("</br>", System.lineSeparator());
    }

    private String getErrorsMessages(String fileName, List<ErrorDescriptor> errors) {
        StringBuilder answer = new StringBuilder();
        for (ErrorDescriptor error : errors) {

            TextPosition errorStart = error.getInterval().getStart();
            answer.append('\n')
                    .append(fileName)
                    .append(' ')
                    .append(errorStart.getLine())
                    .append(':')
                    .append(errorStart.getCh())
                    .append(' ')
                    .append(error.getMessage());
        }
        return answer.toString();
    }

    @Parameterized.Parameters(name = "{index} {0}")
    public static Collection<Object[]> data() throws IOException, URISyntaxException {
        init();
        List<Object[]> parameters = new ArrayList<>();
        for (Example project : ExamplesUtils.getAllExamples(ExamplesFolder.ROOT_FOLDER)) {
            parameters.add(new Object[]{project});
        }
        return parameters;
    }
}

