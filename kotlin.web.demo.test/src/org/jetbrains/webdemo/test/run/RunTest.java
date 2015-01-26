/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.intellij.psi.PsiFile;
import org.jetbrains.webdemo.JetPsiFactoryUtil;
import org.jetbrains.webdemo.responseHelpers.CompileAndRunExecutor;
import org.jetbrains.webdemo.responseHelpers.JsConverter;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.test.BaseTest;
import org.jetbrains.webdemo.test.Common;
import org.jetbrains.webdemo.test.TestUtils;

import java.io.IOException;
import java.security.AccessControlException;
import java.util.Collections;

public class RunTest extends BaseTest {

    public void test$execution$FooOutErr() throws IOException, InterruptedException {
        String expectedResult = "<outStream>Hello</br></outStream><errStream>ERROR</br></errStream>";
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        compareResult(fileName, "", expectedResult, "java");
    }

    public void test$execution$ManyArgs() throws IOException, InterruptedException {
        String expectedResult = "<outStream>a</br>b</br>c</br></outStream>";
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        compareResult(fileName, "a b c", expectedResult, "java");

        compareResult(fileName, "\"a\" b c", expectedResult, "java");
        expectedResult = "<outStream>a b</br>c</br></outStream>";
        compareResult(fileName, "\"a b\" c", expectedResult, "java");
        expectedResult = "";
        compareResult(fileName, "", expectedResult, "java");

        //"info"},{"text":"a \["Hello]\" b<br/>c<br/>","ty...>
        //"info"},{"text":"a \[\\"Hello\\]\" b<br/>c<br/>","ty

        //expectedResult = "[{\"text\":\"Generated classfiles: <br/>_DefaultPackage.class<br/>\",\"type\":\"info\"},{\"text\":\"a \\\"Hello\\\" b<br/>c<br/>\",\"type\":\"out\"}]";
        //compareResult(fileName, "\"a \\\"Hello\\\" b\" c", expectedResult, "java");
    }

    public void test$execution$FooOut() throws IOException, InterruptedException {
        String expectedResult = "<outStream>Hello</br></outStream>";
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        compareResult(fileName, "", expectedResult, "java");
    }

    public void test$execution$FooErr() throws IOException, InterruptedException {
        String expectedResult = "<errStream>ERROR</br></errStream>";
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        compareResult(fileName, "", expectedResult, "java");
    }

    //Runtime.getRuntime().exec() Exception
    public void test$errors$securityExecutionError() throws IOException, InterruptedException {
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        checkException(fileName, "", "java", AccessControlException.class.getName());
    }

    private void checkException(String fileName, String args, String runConfiguration, String exceptionName) throws IOException {
        sessionInfo.setType(SessionInfo.TypeOfRequest.RUN);
        sessionInfo.setRunConfiguration(runConfiguration);

        if (sessionInfo.getRunConfiguration().equals(SessionInfo.RunConfiguration.JAVA)) {
            PsiFile currentPsiFile = JetPsiFactoryUtil.createFile(getProject(), getProject().getName(), TestUtils.getDataFromFile(TestUtils.TEST_SRC, fileName));
            sessionInfo.setType(SessionInfo.TypeOfRequest.RUN);

            CompileAndRunExecutor responseForCompilation = new CompileAndRunExecutor(Collections.singletonList(currentPsiFile), currentPsiFile.getProject(), sessionInfo, args);
            ArrayNode actualResult = (ArrayNode) new ObjectMapper().readTree(responseForCompilation.getResult());
            for (JsonNode outputObject : actualResult) {
                if (outputObject.get("type").asText().equals("out")) {
                    assertEquals(actualResult.get(1).get("exception").get("fullName").asText(), exceptionName);
                }
            }
        }
    }


    //Exception when read file from other directory
    public void test$errors$securityFilePermissionError() throws IOException, InterruptedException {
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        checkException(fileName, "", "java", AccessControlException.class.getName());
    }

    private void compareResult(String fileName, String args, String expectedResult, String runConfiguration) throws IOException {
        sessionInfo.setType(SessionInfo.TypeOfRequest.RUN);
        sessionInfo.setRunConfiguration(runConfiguration);

        if (sessionInfo.getRunConfiguration().equals(SessionInfo.RunConfiguration.JAVA)) {
            PsiFile currentPsiFile = JetPsiFactoryUtil.createFile(getProject(), getProject().getName(), TestUtils.getDataFromFile(TestUtils.TEST_SRC, fileName));
            sessionInfo.setType(SessionInfo.TypeOfRequest.RUN);

            CompileAndRunExecutor responseForCompilation = new CompileAndRunExecutor(Collections.singletonList(currentPsiFile), currentPsiFile.getProject(), sessionInfo, args);
            ArrayNode actualResult = (ArrayNode) new ObjectMapper().readTree(responseForCompilation.getResult());
            for (JsonNode outputObject : actualResult) {
                if (outputObject.get("type").asText().equals("out")) {
                    String outputText = Common.unEscapeString(outputObject.get("text").asText().replaceAll(System.lineSeparator(), "</br>"));
                    assertEquals(expectedResult, outputText);
                    assertTrue(outputObject.get("exception").isNull());
                }
            }
        } else {
            sessionInfo.setType(SessionInfo.TypeOfRequest.CONVERT_TO_JS);
            PsiFile currentPsiFile = JetPsiFactoryUtil.createFile(getProject(), getProject().getName(), TestUtils.getDataFromFile(TestUtils.TEST_SRC, fileName));
            String actualResult = new JsConverter(sessionInfo).getResult(Collections.singletonList(currentPsiFile), args);
            assertEquals("wrong result", expectedResult, actualResult);
        }
    }
}
