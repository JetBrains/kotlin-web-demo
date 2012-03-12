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

import org.jetbrains.jet.lang.psi.JetFile;
import org.jetbrains.jet.lang.psi.JetPsiFactory;
import org.jetbrains.webdemo.Initializer;
import org.jetbrains.webdemo.responseHelpers.JsonResponseForHighlighting;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.test.BaseTest;
import org.jetbrains.webdemo.test.TestUtils;

import java.io.IOException;

/**
 * @author Natalia.Ukhorskaya
 */

public class HighlightingTest extends BaseTest {

    public void test$errors$oneError() throws IOException, InterruptedException {
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        String expectedResult = "[{\"titleName\":\"Only safe calls (?.) are allowed on a nullable receiver of type PrintStream?\"," +
                "\"className\":\"red_wavy_line\"," +
                "\"severity\":\"ERROR\"," +
                "\"y\":\"{line: 1, ch: 13}\"," +
                "\"x\":\"{line: 1, ch: 12}\"}]";
        compareResponseAndExpectedResult(fileName, expectedResult, "java");
        /*expectedResult = "[{\"titleName\":\"Unresolved reference: System\"," +
                "\"className\":\"ERROR\"," +
                "\"severity\":\"ERROR\"," +
                "\"y\":\"{line: 1, ch: 8}\"," +
                "\"x\":\"{line: 1, ch: 2}\"}]";
        compareResponseAndExpectedResult(fileName, expectedResult, "js");*/
    }

    public void test$errors$oneError$js() throws IOException, InterruptedException {
        String fileName = TestUtils.getNameByTestName(this).substring(0, TestUtils.getNameByTestName(this).length() - 3) + ".kt";
        String expectedResult = "[{\"titleName\":\"Unresolved reference: System\"," +
                "\"className\":\"ERROR\"," +
                "\"severity\":\"ERROR\"," +
                "\"y\":\"{line: 1, ch: 8}\"," +
                "\"x\":\"{line: 1, ch: 2}\"}]";
        compareResponseAndExpectedResult(fileName, expectedResult, "js");
    }

    public void test$warnings$oneWarning() throws IOException, InterruptedException {
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        String expectedResult = "[{\"titleName\":\"Unnecessary safe call on a non-null receiver of type Int\"," +
                "\"className\":\"WARNING\"," +
                "\"severity\":\"WARNING\"," +
                "\"y\":\"{line: 2, ch: 7}\"," +
                "\"x\":\"{line: 2, ch: 5}\"}]";
        compareResponseAndExpectedResult(fileName, expectedResult, "java");
    }

    public void test$errors$twoErrorsInOneLine() throws IOException, InterruptedException {
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        String expectedResult = "[{\"titleName\":\"Only safe calls (?.) are allowed on a nullable receiver of type PrintStream?\"," +
                "\"className\":\"red_wavy_line\"," +
                "\"severity\":\"ERROR\"," +
                "\"y\":\"{line: 1, ch: 15}\"," +
                "\"x\":\"{line: 1, ch: 14}\"}," +
                "{\"titleName\":\"Expecting ')'\"," +
                "\"className\":\"red_wavy_line\"," +
                "\"severity\":\"ERROR\"," +
                "\"y\":\"{line: 1, ch: 30}\"," +
                "\"x\":\"{line: 1, ch: 29}\"}]";
        compareResponseAndExpectedResult(fileName, expectedResult, "java");
    }

    private void compareResponseAndExpectedResult(String fileName, String expectedResult, String runConfiguration) throws IOException {
        sessionInfo.setType(SessionInfo.TypeOfRequest.HIGHLIGHT);
        sessionInfo.setRunConfiguration(runConfiguration);
        JetFile currentPsiFile = JetPsiFactory.createFile(Initializer.INITIALIZER.getEnvironment().getProject(), TestUtils.getDataFromFile(TestUtils.TEST_SRC, fileName));
        JsonResponseForHighlighting responseForHighlighting = new JsonResponseForHighlighting(currentPsiFile, sessionInfo);
        String actualResult = responseForHighlighting.getResult();

        assertEquals("Wrong result", expectedResult, actualResult);
    }

}
