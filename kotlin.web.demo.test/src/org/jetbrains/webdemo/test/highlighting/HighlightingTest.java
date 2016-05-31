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

package org.jetbrains.webdemo.test.highlighting;

import com.intellij.psi.PsiFile;
import org.jetbrains.webdemo.backend.BackendSessionInfo;
import org.jetbrains.webdemo.backend.JetPsiFactoryUtil;
import org.jetbrains.webdemo.backend.responseHelpers.JsonResponseForHighlighting;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.test.BaseTest;
import org.jetbrains.webdemo.test.TestUtils;

import java.io.IOException;
import java.util.Collections;

public class HighlightingTest extends BaseTest {

    public void test$errors$oneError() throws IOException, InterruptedException {
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        String expectedResult = "{\"" + fileName + "\":[" +
                "{" +
                "\"interval\":{\"start\":{\"line\":1,\"ch\":2},\"end\":{\"line\":1,\"ch\":8}}," +
                "\"message\":\"Unresolved reference: prntln\"," +
                "\"severity\":\"ERROR\"," +
                "\"className\":\"ERROR\"" +
                "}" +
                "]}";
        compareResponseAndExpectedResult(fileName, expectedResult, "java");
    }

    public void test$errors$oneError$js() throws IOException, InterruptedException {
        String fileName = TestUtils.getNameByTestName(this).substring(0, TestUtils.getNameByTestName(this).length() - 3) + ".kt";
        String expectedResult = "{\"" + fileName + "\":[" +
                "{" +
                "\"interval\":{\"start\":{\"line\":1,\"ch\":2},\"end\":{\"line\":1,\"ch\":8}}," +
                "\"message\":\"Unresolved reference: prntln\"," +
                "\"severity\":\"ERROR\"," +
                "\"className\":\"ERROR\"" +
                "}" +
                "]}";
        compareResponseAndExpectedResult(fileName, expectedResult, "js");
    }

    public void test$warnings$oneWarning() throws IOException, InterruptedException {
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        String expectedResult = "{\"" + fileName + "\":[" +
                "{" +
                "\"interval\":{\"start\":{\"line\":2,\"ch\":5},\"end\":{\"line\":2,\"ch\":7}}," +
                "\"message\":\"Unnecessary safe call on a non-null receiver of type Int\"," +
                "\"severity\":\"WARNING\"," +
                "\"className\":\"WARNING\"" +
                "}" +
                "]}";
        compareResponseAndExpectedResult(fileName, expectedResult, "java");
    }

    public void test$errors$twoErrorsInOneLine() throws IOException, InterruptedException {
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        String expectedResult = "{\"" + fileName + "\":[" +
                "{" +
                "\"interval\":{\"start\":{\"line\":1,\"ch\":2},\"end\":{\"line\":1,\"ch\":8}}," +
                "\"message\":\"Unresolved reference: prntln\"," +
                "\"severity\":\"ERROR\"," +
                "\"className\":\"ERROR\"" +
                "}" +
                "," +
                "{" +
                "\"interval\":{\"start\":{\"line\":1,\"ch\":15},\"end\":{\"line\":1,\"ch\":16}}," +
                "\"message\":\"Expecting ')'\"," +
                "\"severity\":\"ERROR\"," +
                "\"className\":\"red_wavy_line\"" +
                "}" +
                "]}";
        compareResponseAndExpectedResult(fileName, expectedResult, "java");
    }

    private void compareResponseAndExpectedResult(String fileName, String expectedResult, String runConfiguration) throws IOException {
        BackendSessionInfo sessionInfo = new BackendSessionInfo("test", BackendSessionInfo.TypeOfRequest.HIGHLIGHT);
        sessionInfo.setRunConfiguration(runConfiguration);
        PsiFile currentPsiFile = JetPsiFactoryUtil.createFile(getProject(), fileName, TestUtils.getDataFromFile(TestUtils.TEST_SRC, fileName));
        JsonResponseForHighlighting responseForHighlighting = new JsonResponseForHighlighting(Collections.singletonList(currentPsiFile), sessionInfo, currentPsiFile.getProject());
        String actualResult = responseForHighlighting.getResult();

        assertEquals("Wrong result", expectedResult, actualResult);
    }

}
