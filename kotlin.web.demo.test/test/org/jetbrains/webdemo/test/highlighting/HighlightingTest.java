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

import org.jetbrains.webdemo.kotlin.datastructures.ErrorDescriptor;
import org.jetbrains.webdemo.kotlin.datastructures.Severity;
import org.jetbrains.webdemo.kotlin.datastructures.TextInterval;
import org.jetbrains.webdemo.kotlin.datastructures.TextPosition;
import org.jetbrains.webdemo.test.BaseTest;
import org.jetbrains.webdemo.test.TestUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HighlightingTest extends BaseTest {

    public void test$errors$oneError() throws IOException, InterruptedException {
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        List<ErrorDescriptor> expectedResult = new ArrayList<>();
        expectedResult.add(new ErrorDescriptor(
                new TextInterval(new TextPosition(1, 2), new TextPosition(1,8)),
                "Unresolved reference: prntln",
                Severity.ERROR,
                null
        ));
        compareResponseAndExpectedResult(fileName, expectedResult, false);
    }

    public void test$errors$oneError$js() throws IOException, InterruptedException {
        String fileName = TestUtils.getNameByTestName(this).substring(0, TestUtils.getNameByTestName(this).length() - 3) + ".kt";
        List<ErrorDescriptor> expectedResult = new ArrayList<>();
        expectedResult.add(new ErrorDescriptor(
                new TextInterval(new TextPosition(1,2), new TextPosition(1,8)),
                "Unresolved reference: prntln",
                Severity.ERROR,
                null
        ));
        compareResponseAndExpectedResult(fileName, expectedResult, true);
    }

    public void test$warnings$oneWarning() throws IOException, InterruptedException {
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        List<ErrorDescriptor> expectedResult = new ArrayList<>();
        expectedResult.add(new ErrorDescriptor(
                new TextInterval(new TextPosition(2, 5), new TextPosition(2, 7)),
                "Unnecessary safe call on a non-null receiver of type kotlin.Int",
                Severity.WARNING,
                null
        ));
        compareResponseAndExpectedResult(fileName, expectedResult, false);
    }

    public void test$errors$twoErrorsInOneLine() throws IOException, InterruptedException {
        String fileName = TestUtils.getNameByTestName(this) + ".kt";
        List<ErrorDescriptor> expectedResult = new ArrayList<>();
        expectedResult.add(new ErrorDescriptor(
                new TextInterval(new TextPosition(1,2), new TextPosition(1,8)),
                "Unresolved reference: prntln",
                Severity.ERROR,
                null
        ));
        expectedResult.add(new ErrorDescriptor(
                new TextInterval(new TextPosition(1,15), new TextPosition(1,16)),
                "Expecting ')'",
                Severity.ERROR,
                "red_wavy_line"
        ));
        compareResponseAndExpectedResult(fileName, expectedResult, false);
    }

    private void compareResponseAndExpectedResult(String fileName, List<ErrorDescriptor> expectedResult, boolean isJs)
            throws IOException {
        String fileContent = TestUtils.getDataFromFile(fileName);
        Map<String, String> files = new HashMap<>();
        files.put(fileName, fileContent);
        List<ErrorDescriptor> actualErrors = kotlinWrapper.getErrors(files, isJs).get(fileName);

        for (ErrorDescriptor expectedError : expectedResult) {
            assertTrue(actualErrors.contains(expectedError));
        }

        for (ErrorDescriptor actualError : actualErrors){
            assertTrue(expectedResult.contains(actualError));
        }
    }

}
