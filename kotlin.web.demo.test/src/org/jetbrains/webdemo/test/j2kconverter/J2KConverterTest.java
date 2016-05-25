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

import org.jetbrains.webdemo.test.BaseTest;
import org.jetbrains.webdemo.test.TestUtils;

import java.io.IOException;

public class J2KConverterTest extends BaseTest {

    public void test$j2kconverter$class() throws IOException {
        String javaCodeFileName = TestUtils.getNameByTestName(this) + ".java";
        String kotlinCodeFileName = TestUtils.getNameByTestName(this) + ".kt";
        compareResult(javaCodeFileName, kotlinCodeFileName);
    }

    private void compareResult(String javaCodeFileName, String kotlinCodeFileName) throws IOException {
        String javaCode = TestUtils.getDataFromFile(javaCodeFileName);
        String expectedResult = TestUtils.getDataFromFile(kotlinCodeFileName);
        String actualResult = kotlinWrapper.translateJavaToKotlin(javaCode);

        assertEquals("wrong result in " + javaCodeFileName, expectedResult, actualResult + "\n");
    }
}
