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

import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.psi.JetPsiFactory;
import org.jetbrains.webdemo.Initializer;
import org.jetbrains.webdemo.responseHelpers.CompileAndRunExecutor;
import org.jetbrains.webdemo.responseHelpers.JsConverter;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.test.BaseTest;
import org.jetbrains.webdemo.test.TestUtils;

import java.io.IOException;

/**
 * @author Natalia.Ukhorskaya
 */

public class RunTest extends BaseTest {

    public void test$execution$FooOutErr() throws IOException, InterruptedException {
        String expectedResult = "[{\"text\":\"Generated classfiles: <br/>namespace.class<br/>\",\"type\":\"info\"},{\"text\":\"Hello<br/>\",\"type\":\"out\"},{\"text\":\"ERROR<br/>\",\"type\":\"err\"}]";
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        compareResult(fileName, "", expectedResult, "java");
    }

    public void test$execution$FooOut() throws IOException, InterruptedException {
        String expectedResult = "[{\"text\":\"Generated classfiles: <br/>namespace.class<br/>\",\"type\":\"info\"},{\"text\":\"Hello<br/>\",\"type\":\"out\"}]";
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        compareResult(fileName, "", expectedResult, "java");
    }

    public void test$execution$FooErr() throws IOException, InterruptedException {
        String expectedResult = "[{\"text\":\"Generated classfiles: <br/>namespace.class<br/>\",\"type\":\"info\"},{\"text\":\"\",\"type\":\"out\"},{\"text\":\"ERROR<br/>\",\"type\":\"err\"}]";
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        compareResult(fileName, "", expectedResult, "java");
    }

    //Runtime.getRuntime().exec() Exception
    public void test$errors$securityExecutionError() throws IOException, InterruptedException {
        String expectedResult = "Exception in thread \\\"main\\\" java.security.AccessControlException: access denied (java.io.FilePermission &amp;lt;&amp;lt;ALL FILES&amp;gt;&amp;gt; execute)";
//        String expectedResult = "[{\"text\":\"Generated classfiles: <br/>namespace.class<br/>\",\"type\":\"info\"},{\"text\":\"\",\"type\":\"out\"},{\"text\":\"Exception in thread \\\"main\\\" java.security.AccessControlException: access denied (java.io.FilePermission &amp;lt;&amp;lt;ALL FILES&amp;gt;&amp;gt; execute)<br/>\\tat java.security.AccessControlContext.checkPermission(AccessControlContext.java:374)<br/>\\tat java.security.AccessController.checkPermission(AccessController.java:546)<br/>\\tat java.lang.SecurityManager.checkPermission(SecurityManager.java:532)<br/>\\tat java.lang.SecurityManager.checkExec(SecurityManager.java:782)<br/>\\tat java.lang.ProcessBuilder.start(ProcessBuilder.java:448)<br/>\\tat java.lang.Runtime.exec(Runtime.java:593)<br/>\\tat java.lang.Runtime.exec(Runtime.java:431)<br/>\\tat java.lang.Runtime.exec(Runtime.java:328)<br/>\\tat namespace.main(dummy.jet:2)<br/>\",\"type\":\"err\"}]";
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        compareResult(fileName, "", expectedResult, "java");
    }


    //Exception when read file from other directory
    public void test$errors$securityFilePermissionError() throws IOException, InterruptedException {
        String expectedResult = "Exception in thread \\\"main\\\" java.security.AccessControlException: access denied (java.io.FilePermission test.kt read)";
//        String expectedResult = "[{\"text\":\"Generated classfiles: <br/>namespace.class<br/>\",\"type\":\"info\"},{\"text\":\"\",\"type\":\"out\"},{\"text\":\"Exception in thread \\\"main\\\" java.security.AccessControlException: access denied (java.io.FilePermission test.kt read)<br/>\\tat java.security.AccessControlContext.checkPermission(AccessControlContext.java:374)<br/>\\tat java.security.AccessController.checkPermission(AccessController.java:546)<br/>\\tat java.lang.SecurityManager.checkPermission(SecurityManager.java:532)<br/>\\tat java.lang.SecurityManager.checkRead(SecurityManager.java:871)<br/>\\tat java.io.File.exists(File.java:731)<br/>\\tat namespace.main(dummy.jet:3)<br/>\",\"type\":\"err\"}]";
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        compareResult(fileName, "", expectedResult, "java");
    }

    private void compareResult(String fileName, String args, String expectedResult, String runConfiguration) throws IOException {
        sessionInfo.setType(SessionInfo.TypeOfRequest.RUN);
        sessionInfo.setRunConfiguration(runConfiguration);

        if (sessionInfo.getRunConfiguration().equals(SessionInfo.RunConfiguration.JAVA)) {
            JetFile currentPsiFile = JetPsiFactory.createFile(Initializer.INITIALIZER.getEnvironment().getProject(), TestUtils.getDataFromFile(TestUtils.TEST_SRC, fileName));
            sessionInfo.setType(SessionInfo.TypeOfRequest.RUN);

            CompileAndRunExecutor responseForCompilation = new CompileAndRunExecutor(currentPsiFile, args, sessionInfo);
            String actualResult = responseForCompilation.getResult();
            if (fileName.endsWith("securityExecutionError.kt") || fileName.endsWith("securityFilePermissionError.kt")) {
                assertTrue("Wrong result", actualResult.contains(expectedResult));
            } else {
                assertEquals("Wrong result", expectedResult, actualResult);
            }
        } else {
            sessionInfo.setType(SessionInfo.TypeOfRequest.CONVERT_TO_JS);
            String actualResult = new JsConverter(sessionInfo).getResult(TestUtils.getDataFromFile(TestUtils.TEST_SRC, fileName), args);
            assertEquals("wrong result", expectedResult, actualResult);
        }
    }
}
