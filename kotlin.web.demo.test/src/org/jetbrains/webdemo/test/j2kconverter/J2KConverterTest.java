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

package org.jetbrains.webdemo.test.j2kconverter;

import com.intellij.psi.PsiFile;
import org.jetbrains.webdemo.backend.BackendSessionInfo;
import org.jetbrains.webdemo.backend.JetPsiFactoryUtil;
import org.jetbrains.webdemo.backend.errorsDescriptors.ErrorAnalyzer;
import org.jetbrains.webdemo.backend.responseHelpers.WebDemoJavaToKotlinConverter;
import org.jetbrains.webdemo.test.BaseTest;
import org.jetbrains.webdemo.test.TestUtils;

import java.io.IOException;
import java.util.Collections;

public class J2KConverterTest extends BaseTest {

    public void test$j2kconverter$class() throws IOException {
        String fileName = TestUtils.getNameByTestName(this) + ".java";
        String expectedResult = "[{\"text\":\"class Foo {\\ninternal fun bar() {}\\ninternal fun foo():Int {\\nreturn 0\\n}\\n}\"}]";

        assertTrue(true);
        compareResult(fileName, expectedResult);
    }

    private void compareResult(String fileName, String expectedResult) throws IOException {
        BackendSessionInfo sessionInfo = new BackendSessionInfo("test", BackendSessionInfo.TypeOfRequest.CONVERT_TO_KOTLIN);
        PsiFile currentPsiFile = JetPsiFactoryUtil.createFile(getProject(), fileName, "class A");
        new ErrorAnalyzer(Collections.singletonList(currentPsiFile), sessionInfo, currentPsiFile.getProject()).getAllErrors();
        String actualResult = new WebDemoJavaToKotlinConverter(sessionInfo).getResult(TestUtils.getDataFromFile(TestUtils.TEST_SRC, fileName), currentPsiFile.getProject());
        assertEquals("wrong result in " + fileName, expectedResult, actualResult);
    }
}
