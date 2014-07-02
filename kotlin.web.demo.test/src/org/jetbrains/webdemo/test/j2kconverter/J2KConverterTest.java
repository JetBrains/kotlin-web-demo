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

import org.jetbrains.webdemo.responseHelpers.JavaToKotlinConverter;
import org.jetbrains.webdemo.session.SessionInfo;
import org.jetbrains.webdemo.test.BaseTest;
import org.jetbrains.webdemo.test.TestUtils;

import java.io.IOException;

public class J2KConverterTest extends BaseTest {

    public void test$j2kconverter$class() throws IOException {
        String fileName = TestUtils.getNameByTestName(this) + ".java";
        String expectedResult = "[{\"text\":\"public class Foo()\"}]";

        assertTrue(true);
        compareResult(fileName, expectedResult);
    }

    private void compareResult(String fileName, String expectedResult) throws IOException {
        sessionInfo.setType(SessionInfo.TypeOfRequest.CONVERT_TO_KOTLIN);

        String actualResult = new JavaToKotlinConverter(sessionInfo).getResult(TestUtils.getDataFromFile(TestUtils.TEST_SRC, fileName));
        assertEquals("wrong result in " + fileName, expectedResult, actualResult);
    }
}
