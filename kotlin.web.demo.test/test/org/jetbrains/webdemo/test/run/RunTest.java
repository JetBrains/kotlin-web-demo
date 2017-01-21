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

package org.jetbrains.webdemo.test.run;

import org.jetbrains.webdemo.backend.executor.ExecutorUtils;
import org.jetbrains.webdemo.backend.executor.result.JavaExecutionResult;
import org.jetbrains.webdemo.kotlin.KotlinWrappersManager;
import org.jetbrains.webdemo.kotlin.datastructures.CompilationResult;
import org.jetbrains.webdemo.test.BaseTest;
import org.jetbrains.webdemo.test.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.*;

@RunWith(Parameterized.class)
public class RunTest extends BaseTest {

    public RunTest(String kotlinVersion) {
        super(kotlinVersion);
    }

    @Test
    public void test$execution$FooOutErr() throws Exception {
        String expectedResult = "<outStream>Hello\n</outStream><errStream>ERROR\n</errStream>";
        String fileName = TestUtils.getNameByTestName(name.getMethodName()) + ".kt";
        compareResult(fileName, "", expectedResult);
    }

    @Test
    public void test$execution$ManyArgs() throws Exception {
        String expectedResult = "<outStream>a\nb\nc\n</outStream>";
        String fileName = TestUtils.getNameByTestName(name.getMethodName()) + ".kt";
        compareResult(fileName, "a b c", expectedResult);

        compareResult(fileName, "\"a\" b c", expectedResult);
        expectedResult = "<outStream>a b\nc\n</outStream>";
        compareResult(fileName, "\"a b\" c", expectedResult);
        expectedResult = "";
        compareResult(fileName, "", expectedResult);
    }

    @Test
    public void test$execution$FooOut() throws Exception {
        String expectedResult = "<outStream>Hello\n</outStream>";
        String fileName = TestUtils.getNameByTestName(name.getMethodName()) + ".kt";
        compareResult(fileName, "", expectedResult);
    }

    @Test
    public void test$execution$Reflection() throws Exception {
        String expectedResult = "<outStream>Any\nA\n</outStream>";
        String fileName = TestUtils.getNameByTestName(name.getMethodName()) + ".kt";
        compareResult(fileName, "", expectedResult);
    }

    @Test
    public void test$execution$FooErr() throws Exception {
        String expectedResult = "<errStream>ERROR\n</errStream>";
        String fileName = TestUtils.getNameByTestName(name.getMethodName()) + ".kt";
        compareResult(fileName, "", expectedResult);
    }

    @Test
    //Runtime.getRuntime().exec() Exception
    public void test$errors$securityExecutionError() throws Exception {
        String fileName = TestUtils.getNameByTestName(name.getMethodName()) + ".kt";
        checkException(fileName, "", AccessControlException.class.getName());
    }

    private void checkException(String fileName, String args, String exceptionName) throws Exception {
        JavaExecutionResult result = compileAndExecute(fileName, args);
        assertEquals(exceptionName, result.getException().getFullName());
    }

    @Test
    //Exception when read file from other directory
    public void test$errors$securityFilePermissionError() throws Exception {
        String fileName = TestUtils.getNameByTestName(name.getMethodName()) + ".kt";
        checkException(fileName, "", AccessControlException.class.getName());
    }

    private void compareResult(String fileName, String args, String expectedResult) throws Exception {
        JavaExecutionResult result = compileAndExecute(fileName, args);
        assertNull(result.getException());
        assertEquals(expectedResult, result.getText());
    }

    private JavaExecutionResult compileAndExecute(String fileName, String args) throws Exception {
        Map<String, String> files = new HashMap<>();
        files.put(fileName, TestUtils.getDataFromFile(fileName));
        CompilationResult compilationResult = kotlinWrapper.compileCorrectFiles(files, fileName);
        return (JavaExecutionResult) ExecutorUtils.executeCompiledFiles(
                compilationResult.getFiles(),
                compilationResult.getMainClass(),
                kotlinWrapper.getKotlinRuntimeLibraries(),
                args,
                kotlinWrapper.getWrapperFolder().resolve("executors.policy"),
                false
        );
    }
}
